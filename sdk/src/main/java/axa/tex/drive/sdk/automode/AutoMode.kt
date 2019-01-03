package axa.tex.drive.sdk.automode

import android.content.Context
import io.reactivex.subjects.PublishSubject

class AutoMode(context: Context) {
    val stateSubject = PublishSubject.create<AutoModeState.State>()

    private var currentState: AutoModeState? = null
    private var formerState: AutoModeState? = null


    fun setCurrentState(currentState: AutoModeState) {
        this.currentState = currentState
        currentState.scan(this)
        stateSubject.onNext(currentState.state())
    }

    fun setFormerState(formerState: AutoModeState) {
        this.formerState = formerState
        formerState.scan(this)
    }

    fun statePublisher(): PublishSubject<AutoModeState.State> {
        return stateSubject
    }
}