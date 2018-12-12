package axa.tex.drive.sdk.acquisition.internal.tracker

import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.MotionFix
import java.util.*

internal  const val DEFAULT_MOTION_AGE_AFTER_ACCELERATION : Int = 5 * 1000
internal const val DEFAULT_OLDER_MOTION_AGE = 10 * 1000

class MotionBuffer {

    private val buffer = LinkedList<Fix>()


    private var olderMotionAge : Int = DEFAULT_OLDER_MOTION_AGE
    private var motionAgeAfterAcceleration : Int = DEFAULT_MOTION_AGE_AFTER_ACCELERATION

    fun addFix(fix : MotionFix?){
        synchronized(buffer){
            if (fix != null) {
                buffer.add(fix)
            }
            val currentDate = System.currentTimeMillis()
            while (buffer.size > 0 && ((currentDate - buffer.first()!!.timestamp()) > olderMotionAge)) {
                buffer.removeFirst()
            }
        }
    }


    fun flush() : List<Fix>{
        synchronized(buffer){
            val currentDate = System.currentTimeMillis()
            while (buffer.size > 0 && ((currentDate - buffer.first()!!.timestamp()) > olderMotionAge)) {
                buffer.removeFirst()
            }

            while (buffer.size > 0 && ((buffer.first().timestamp() - currentDate)  > motionAgeAfterAcceleration)) {
                buffer.removeLast()
            }
            val fixes = buffer.toList()
            buffer.clear()
            return fixes
        }
    }

    internal fun isBufferEmpty() : Boolean{
        return buffer.isEmpty()
    }


    fun setOlderMotionAge(olderMotionAge : Int){
        this.olderMotionAge = olderMotionAge
    }

    fun setMotionAgeAfterAcceleration(motionAgeAfterAcceleration : Int){
        this.motionAgeAfterAcceleration = motionAgeAfterAcceleration
    }

    internal fun getPeriod() : Long{
       return  buffer.last.timestamp() - buffer.first.timestamp()
    }
}