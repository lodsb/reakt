/*
 +1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2011 - 3 - 23 :: 11 : 48
    >>  Origin: mt4j (project) / mt4j_mod (module)
    >>
  +3>>
    >>  Copyright (c) 2011:
    >>
    >>     |             |     |
    >>     |    ,---.,---|,---.|---.
    >>     |    |   ||   |`---.|   |
    >>     `---'`---'`---'`---'`---'
    >>                    // Niklas Klügel
    >>
  +4>>
    >>  Made in Bavaria by fat little elves - since 1983.
 */

package org.lodsb.reaktExt.animation

import org.mt4j.MTApplication
import de.looksgood.ani._
import org.lodsb.reakt.Val
import reaktExt.animation.InterpolatingAnimationTarget
import org.lodsb.reaktExt._
import java.util.{TimerTask, Timer}

// animation state as rich float.... animationFLoat?

sealed abstract class AnimationEasing(val name: String)

case object Linear extends AnimationEasing("linearEasingNone");

case object QuadIn extends AnimationEasing("quadEasingIn");

case object QuadOut extends AnimationEasing("quadEasingOut");

case object QuadInOut extends AnimationEasing("quadEasingInOut");

case object CubicIn extends AnimationEasing("cubicEasingIn");

case object CubicInOut extends AnimationEasing("cubicEasingInOut");

case object CubicOut extends AnimationEasing("cubicEasingOut");

case object QuartIn extends AnimationEasing("quartEasingIn");

case object QuartOut extends AnimationEasing("quartEasingOut");

case object QuartInOut extends AnimationEasing("quartEasingInOut");

case object QuintIn extends AnimationEasing("quintEasingIn");

case object QuintOut extends AnimationEasing("quintEasingOut");

case object QuintInOut extends AnimationEasing("quintEasingInOut");

case object SineIn extends AnimationEasing("sineEasingIn");

case object SineOut extends AnimationEasing("sineEasingOut");

case object SineInOut extends AnimationEasing("sineEasingInOut");

case object CircIn extends AnimationEasing("circEasingIn");

case object CircOut extends AnimationEasing("circEasingOut");

case object CircInOut extends AnimationEasing("circEasingInOut");

case object ExpIn extends AnimationEasing("expoEasingIn");

case object ExpOut extends AnimationEasing("expoEasingOut");

case object ExpInOut extends AnimationEasing("expoEasingInOut");

case object BackIn extends AnimationEasing("backEasingIn");

case object BackOut extends AnimationEasing("backEasingOut");

case object BackInOut extends AnimationEasing("backEasingInOut");

case object BounceIn extends AnimationEasing("bounceEasingIn");

case object BounceOut extends AnimationEasing("bounceEasingOut");

case object BounceInOut extends AnimationEasing("bounceEasingInOut");

case object ElasicIn extends AnimationEasing("elasticEasingIn");

case object ElasticOut extends AnimationEasing("elasticEasingOut");

case object ElasticInOut extends AnimationEasing("elasticEasingInOut");

trait Animation

object Animation {
	private val timer = new Timer

	protected[animation] def scheduleAnimation(animation: InterpolatingAnimation, msecs: Long): TimerTask = {
		val task = new TimerTask {
			def run {
				animation.updateAnimation
			}
		}

		timer.schedule(task, 0, msecs)

		task
	}

	private var isAniInitialized = false;

	protected[animation] def createNewInterpolatingAnimation(target: Any, field: String, to: Float,
															 duration: Float = 60f, delay: Float = 0f,
															 easing: String): Ani = {

		if(!isAniInitialized) {Ani.init(MTApplication.getInstance); System.err.println(MTApplication.getInstance)}
		new Ani(target, duration, delay, field, to, easing)
	}

}

class InterpolatingAnimation(from: Float, to: Float, duration: Float = 60f, easing: AnimationEasing = Linear,
							 repeat: Int = 0, delay: Float = 0.0f, timeResolutionMsecs: Long = 50)

	extends Val[Float](from)
	with Animation {

	private val target = new InterpolatingAnimationTarget();
	private val ani = Animation.createNewInterpolatingAnimation(target, "value", to, duration, delay, easing.name)
	private var timerTask: TimerTask = _

	target.value = from;

	ani.setBegin(from)
	ani.repeat(repeat)

	// scheduler....

	override def start = {
		this.timerTask = Animation.scheduleAnimation(this, timeResolutionMsecs)
		ani.start

		super.start
	}

	def stop = {
		this.timerTask.cancel
	}

	protected[animation] def updateAnimation = {
		this.emit(target.value)
	}
}

