import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import kotlin.Exception

sealed class ShapeDrawable {
    var next: ShapeDrawable? = null

    abstract fun box(drawable: GradientDrawable?): GradientDrawable

    class Solid(val color: Any) : ShapeDrawable() {

        override fun box(drawable: GradientDrawable?): GradientDrawable {
            drawable!!.setColor(color.color)
            return drawable
        }
    }

    class Corner : ShapeDrawable {
        var radiusArray: FloatArray? = null

        var radius: Float = 0.0f

        constructor(radius: Int) : super() {
            this.radius = radius.toFloat()
        }

        constructor(
            topLeftRadius: Int,
            topRightRadius: Int,
            bottomRightRidus: Int,
            bottomLeftRidus: Int
        ) {
            radiusArray = floatArrayOf(
                topLeftRadius.toFloat(), topLeftRadius.toFloat(),
                topRightRadius.toFloat(), topRightRadius.toFloat(),
                bottomRightRidus.toFloat(), bottomRightRidus.toFloat(),
                bottomLeftRidus.toFloat(), bottomLeftRidus.toFloat()
            )
        }

        override fun box(drawable: GradientDrawable?): GradientDrawable {
            if (radiusArray == null) {
                drawable!!.cornerRadius = radius
            } else {
                drawable!!.cornerRadii = radiusArray
            }
            return drawable
        }
    }

    data class Stroke(
        val strokeWidth: Int,
        val dashColor: Any,
        val dashWidth: Int = 0,
        val dashGap: Int = 0,
        val shapeType: Int = GradientDrawable.RECTANGLE
    ) : ShapeDrawable() {

        override fun box(drawable: GradientDrawable?): GradientDrawable {
            drawable!!.apply {
                setStroke(
                    strokeWidth.dp.toInt(),
                    dashColor.color,
                    dashWidth.dp,
                    dashGap.dp
                )
                shape = shapeType
            }

            return drawable
        }
    }


    data class GradientState(
        val orientation: GradientDrawable.Orientation,
        val startColor: Any,
        val endColor: Any
    ) : ShapeDrawable() {

        override fun box(drawable: GradientDrawable?): GradientDrawable {
            //因为这个是new 出来的,所以.这个调用需要在第一个
            return GradientDrawable(
                orientation, intArrayOf(
                    startColor.color,
                    endColor.color
                )
            )
        }
    }

    operator fun plus(shape: ShapeDrawable): ShapeDrawable {
        shape.next = this
        return shape
    }

    class Empty : ShapeDrawable() {
        override fun box(drawable: GradientDrawable?): GradientDrawable {
            return GradientDrawable()
        }
    }
}

val Any.color: Int
    get() = when (this) {
        is Int -> this
        is String -> Color.parseColor(this)
        else -> throw Exception("please input color")
    }

val Int.dp: Float
    get() = (this * 3).toFloat()


fun solid(color: Any): ShapeDrawable.Solid {
    return ShapeDrawable.Solid(color)
}

fun corner(radius: Int): ShapeDrawable.Corner {
    return ShapeDrawable.Corner(radius)
}

fun corner(
    topLeftRadius: Int,
    topRightRadius: Int,
    bottomRightRidus: Int,
    bottomLeftRidus: Int
): ShapeDrawable.Corner {
    return ShapeDrawable.Corner(topLeftRadius, topRightRadius, bottomRightRidus, bottomLeftRidus)
}

fun stroke(
    strokeWidth: Int,
    dashColor: Any,
    dashWidth: Int = 0,
    dashGap: Int = 0,
    shapeType: Int = GradientDrawable.RECTANGLE
): ShapeDrawable.Stroke {
    return ShapeDrawable.Stroke(strokeWidth, dashColor, dashWidth, dashGap, shapeType)
}

fun gradient(
    orientation: GradientDrawable.Orientation,
    startColor: Any,
    endColor: Any
): ShapeDrawable.GradientState {
    return ShapeDrawable.GradientState(orientation, startColor, endColor)
}

var View.shape: ShapeDrawable
    get() = ShapeDrawable.Empty()
    set(value) {
        var s: ShapeDrawable? = value
        val list = mutableListOf<ShapeDrawable>()
        var drawable: GradientDrawable? = null
        while (s != null) {
            if (s is ShapeDrawable.GradientState) {
                drawable = s.box(null)
            } else {
                list.add(s)
            }
            s = s.next
        }

        if (drawable == null) {
            drawable = GradientDrawable()
        }

        list.forEach {
            it.box(drawable)
        }

        background = drawable
    }
