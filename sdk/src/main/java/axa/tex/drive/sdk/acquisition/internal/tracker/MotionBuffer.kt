package axa.tex.drive.sdk.acquisition.internal.tracker

import axa.tex.drive.sdk.acquisition.model.Data
import axa.tex.drive.sdk.acquisition.model.MotionFix
import java.util.*

const val DEFAULT_MOTION_AGE_AFTER_ACCELERATION : Int = 5 * 1000
const val DEFAULT_OLDER_MOTION_AGE = 10 * 1000

class MotionBuffer {

    private val buffer = LinkedList<Data>()


    private var olderMotionAge : Int = DEFAULT_OLDER_MOTION_AGE
    private var motionAgeAfterAcceleration : Int = DEFAULT_MOTION_AGE_AFTER_ACCELERATION

    fun addFix(fix : MotionFix?){
        synchronized(buffer){
            if (fix != null) {
                buffer.add(Data(fix.timestamp, motion = fix))
            }
            val currentDate = System.currentTimeMillis()
            while (buffer.size > 0 && ((currentDate - buffer.first().motion!!.timestamp()) > olderMotionAge)) {
                buffer.removeFirst()
            }
        }
    }


    fun flush() : List<Data>{
        synchronized(buffer){
            val currentDate = System.currentTimeMillis()
            while (buffer.size > 0 && ((currentDate - buffer.first().motion!!.timestamp()) > olderMotionAge)) {
                buffer.removeFirst()
            }

            while (buffer.size > 0 && ((buffer.first().motion!!.timestamp() - currentDate)  > motionAgeAfterAcceleration)) {
                buffer.removeLast()
            }
            val data = buffer.toList()
            buffer.clear()
            return data
        }
    }


    fun setOlderMotionAge(olderMotionAge : Int){
        this.olderMotionAge = olderMotionAge
    }

    fun setMotionAgeAfterAcceleration(motionAgeAfterAcceleration : Int){
        this.motionAgeAfterAcceleration = motionAgeAfterAcceleration
    }
}