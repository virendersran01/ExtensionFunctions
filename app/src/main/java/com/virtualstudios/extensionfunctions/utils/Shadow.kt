package com.virtualstudios.extensionfunctions.utils

import android.graphics.BlurMaskFilter
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import getColorWithAlpha
import toDp

private fun wrapInCustomShadowWithOffset(
    view: View,
    @ColorRes shadowColor: Int,
) {

    val shadowColorValue = ContextCompat.getColor(view.context, shadowColor)

    val shapeDrawable = ShapeDrawable()
    shapeDrawable.setTint(shadowColorValue)

    //You could use this to set padding directly to the shapeDrawable
    // instead of setting it through the wrapper layout in XML
//        val padding = 14.toDp(view.context.resources)
//        val shapeDrawablePadding = Rect()
//        shapeDrawablePadding.left = padding
//        shapeDrawablePadding.right = padding
//        shapeDrawablePadding.top = padding
//        shapeDrawablePadding.bottom = padding
//        shapeDrawable.setPadding(shapeDrawablePadding)
//        val shadowBlur = padding - 4.toDp(resources)

    //logDebug("vp: ${view.paddingBottom}")
    val shadowBlur = view.paddingBottom - 4.toDp(view.context.resources)
    //logDebug("shadowBlur: $shadowBlur")
    val offset = 4.toDp(view.context.resources)
    //logDebug("offset: $offset")
    //logDebug("radius: ${shadowBlur - offset}")
    shapeDrawable.paint.setShadowLayer(
        shadowBlur - offset, //blur
        offset, //dx
        offset, //dy
        getColorWithAlpha(shadowColorValue, 0.8f) //color
    )
    val filter = BlurMaskFilter(offset, BlurMaskFilter.Blur.OUTER)
    view.setLayerType(View.LAYER_TYPE_SOFTWARE, shapeDrawable.paint)
    shapeDrawable.paint.maskFilter = filter

    val radius = 4.toDp(view.context.resources)
    val outerRadius = floatArrayOf(
        radius, radius, //top-left
        radius, radius, //top-right
        radius, radius, //bottom-right
        radius, radius  //bottom-left
    )
    shapeDrawable.shape = RoundRectShape(outerRadius, null, null)

    val drawable = LayerDrawable(arrayOf<Drawable>(shapeDrawable))
    val inset = view.paddingBottom
    drawable.setLayerInset(
        0,
        inset, //left
        inset, //top
        inset, //right
        inset  //bottom
    )
    view.background = drawable
}


private fun wrapInCustomShadow(
    view: View,
    @ColorRes shadowColor: Int,
) {

    val shadowColorValue = ContextCompat.getColor(view.context, shadowColor)
    val shapeDrawable = ShapeDrawable()
    shapeDrawable.setTint(shadowColorValue)

    val shadowBlur = view.paddingBottom - 4.toDp(view.context.resources)
    shapeDrawable.paint.setShadowLayer(
        shadowBlur,
        0f,
        0f,
        getColorWithAlpha(shadowColorValue, 0.8f)
    )
    view.setLayerType(View.LAYER_TYPE_SOFTWARE, shapeDrawable.paint)

    val radius = 4.toDp(view.context.resources)
    val outerRadius = floatArrayOf(
        radius, radius,
        radius, radius,
        radius, radius,
        radius, radius
    )
    shapeDrawable.shape = RoundRectShape(outerRadius, null, null)

    val drawable = LayerDrawable(arrayOf<Drawable>(shapeDrawable))
    val inset = view.paddingBottom
    drawable.setLayerInset(
        0,
        inset,
        inset,
        inset,
        inset
    )
    view.background = drawable
}