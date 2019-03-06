package axa.tex.drive.sdk.automode.internal.new


import axa.tex.drive.sdk.automode.internal.tracker.model.TexLocation
import axa.tex.drive.sdk.automode.internal.tracker.model.TexActivity
import axa.tex.drive.sdk.automode.internal.tracker.model.TexSpeed
import io.reactivex.subjects.PublishSubject

// 2.8 m/s 10km/h
private const val SPEED_MOVEMENT_THRESHOLD = 10 * 0.28f

// 5.6 m/s 20km/h
private const val SPEED_START_THRESHOLD = 20 * 0.28f

//Accuracy for speed
private const val LOCATION_ACCURACY_THRESHOLD = 20

class SpeedFilter {



    internal val locationInput: PublishSubject<TexSpeed> = PublishSubject.create()
    internal val locationOutputWithAccuracy: PublishSubject<TexSpeed> = PublishSubject.create()
    internal val locationOutputWhatEverTheAccuracy: PublishSubject<TexSpeed> = PublishSubject.create()
    internal val locationOutputUnderStartSpeed: PublishSubject<TexSpeed> = PublishSubject.create()
    internal val locationOutputUnderMovementSpeedWhatEverTheAccuracy: PublishSubject<TexSpeed> = PublishSubject.create()
    internal val locationOutputOverOrEqualsToMovementSpeedWhatEverTheAccuracy: PublishSubject<TexSpeed> = PublishSubject.create()

    internal val activityInput: PublishSubject<TexActivity> = PublishSubject.create()
    internal val activityOutput: PublishSubject<TexActivity> = PublishSubject.create()

    internal val gpsStream: PublishSubject<TexLocation> = PublishSubject.create()


     constructor(requiredSpeedForMovement : Float = SPEED_MOVEMENT_THRESHOLD,
                 requiredSpeedForStart : Float = SPEED_START_THRESHOLD,
                 speedAccuracyLimit : Int = LOCATION_ACCURACY_THRESHOLD){
        locationInput.subscribe {

            if(it.speed < requiredSpeedForMovement){
                locationOutputUnderMovementSpeedWhatEverTheAccuracy.onNext(it)
            }
            if(it.speed >= requiredSpeedForMovement){
                locationOutputOverOrEqualsToMovementSpeedWhatEverTheAccuracy.onNext(it)
            }


            if(it.speed < requiredSpeedForStart){
                locationOutputUnderStartSpeed.onNext(it)
            }
            if(it.speed >= requiredSpeedForMovement){
                locationOutputWhatEverTheAccuracy.onNext(it)
            }
            if(it.speed >= requiredSpeedForMovement && it.accuracy < speedAccuracyLimit){
                locationOutputWithAccuracy.onNext(it);
            }
        }

         activityInput.subscribe {
             activityOutput.onNext(it)
         }
    }

}