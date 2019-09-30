package axa.tex.drive.sdk.acquisition.internal.tracker

import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.MotionFix
import axa.tex.drive.sdk.core.logger.LoggerFactory
import java.util.*

internal const val DEFAULT_MOTION_PERIOD_AFTER_ACCELERATION: Long = 3 * 1000
internal const val DEFAULT_OLDER_MOTION_AGE = 5 * 1000
private const val STRESSED_CAPTURE_RATE_IN_HERTZ = 100

class MotionBuffer {

    internal var afterAcceleration = false
    private val maxBufferSize = STRESSED_CAPTURE_RATE_IN_HERTZ*(DEFAULT_OLDER_MOTION_AGE/1000+DEFAULT_MOTION_PERIOD_AFTER_ACCELERATION/1000)

    private var buffer = mutableListOf<Fix>()
    internal val afterAccelerationBuffer = LinkedList<Fix>()
    internal var crashFix : MotionFix? = null

    internal val logger = LoggerFactory().getLogger(this::class.java.name).logger

    internal var olderMotionAge: Int = DEFAULT_OLDER_MOTION_AGE
    internal var motionPeriodAfterAcceleration: Long = DEFAULT_MOTION_PERIOD_AFTER_ACCELERATION

    @Synchronized fun addFix(fix: MotionFix?) {
            if (fix != null) {
                buffer.add(fix)
            while (buffer.size > 0 && ((buffer.last().timestamp() - buffer.first().timestamp()) > olderMotionAge)) {
                buffer.removeAt(0)
            }
        }

        cleanBuffer()

        logger.info("NUMBER OF MOTIONS : ${buffer.size}", function = "fun addFix(fix: MotionFix?)")
    }


    @Synchronized fun flush(): List<Fix> {
            while (buffer.size > 0 && ((buffer.last().timestamp() - buffer.first().timestamp()) > olderMotionAge)) {
                buffer.removeAt(0)
            }

            while (buffer.size > 0 && ((buffer.first().timestamp() - buffer.last().timestamp()) > motionPeriodAfterAcceleration)) {
                val last = buffer.size-1
                if(last > 0) {
                    buffer.removeAt(last)
                }
            }
            val fixes = buffer.toList()
            buffer.clear()
            return fixes
        //}
    }

    internal fun isBufferEmpty(): Boolean {
        return buffer.isEmpty()
    }





    internal fun getPeriod(): Long {
        val lastIndex = buffer.size-1
        val last = buffer.get(lastIndex)
        if(last != null){
            return 0
        }
        //return buffer.last.timestamp() - buffer.first.timestamp()
        return last.timestamp() - buffer.get(0).timestamp()
    }

     fun addMotionAfter(fix : MotionFix?){
        if(fix != null) {
            afterAccelerationBuffer.add(fix)
        }
    }

    fun flushMotionsAfterAcceleration() : List<Fix>{
        afterAcceleration = false
        val fixes = afterAccelerationBuffer.toList()
        afterAccelerationBuffer.clear()
        return fixes
    }

    fun acquireMotionAfterAcceleration(){
        afterAcceleration = true
    }

    private fun cleanBuffer() {
        if (buffer.size > maxBufferSize) {
            val begin = buffer.size - maxBufferSize
            buffer = buffer.subList(begin.toInt(), (maxBufferSize-1).toInt())
        }
    }

    fun isRelevant(norm: Double): Boolean {
        var oldValue = -1.0
        if(crashFix == null){
            return  true
        }else{
            oldValue = crashFix?.norm()!!
            return oldValue < norm
        }

    }
}