package com.wl.radio.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.GridLayoutManager





class RecyclerSpace : RecyclerView.ItemDecoration{
     var space: Int = 0
     var color = -1
     var type: Int = 1
     var mDivider: Drawable? = null
     var mPaint: Paint? = null

     constructor(space:Int) : super(){
          this.space = space
     }

     constructor(space:Int,color:Int): super(){
          this.space = space
          this.color = color
          mPaint =  Paint(Paint.ANTI_ALIAS_FLAG);
          mPaint!!.setColor(color);
          mPaint!!.setStyle(Paint.Style.FILL);
          mPaint!!.setStrokeWidth((space * 2).toFloat());
     }

     constructor(space: Int,mDivider:Drawable):super(){
          this.space = space;
          this.mDivider = mDivider;
     }

     override fun getItemOffsets(
          outRect: Rect, view: View,
          parent: RecyclerView, state: RecyclerView.State
     ) {
          if (parent.layoutManager != null) {
               if (parent.layoutManager is LinearLayoutManager && parent.layoutManager !is GridLayoutManager) {
                    if ((parent.layoutManager as LinearLayoutManager).orientation == LinearLayoutManager.HORIZONTAL) {
                         outRect.set(space, 0, space, 0)
                    } else {
                         outRect.set(0, space, 0, space)
                    }
               } else {
                    outRect.set(space, space, space, space)
               }
          }

     }

     override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
          super.onDraw(c, parent, state)
          if (parent.layoutManager != null) {
               if (parent.layoutManager is LinearLayoutManager && parent.layoutManager !is GridLayoutManager) {
                    if ((parent.layoutManager as LinearLayoutManager).orientation == LinearLayoutManager.HORIZONTAL) {
                         drawHorizontal(c, parent)
                    } else {
                         drawVertical(c, parent)
                    }
               } else {
                    if (type === 0) {
                         drawGrideview(c, parent)
                    } else {
                         drawGrideview1(c, parent)
                    }
               }
          }
     }


     //绘制纵向 item 分割线

     private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
          val top = parent.paddingTop
          val bottom = parent.measuredHeight - parent.paddingBottom
          val childSize = parent.childCount
          for (i in 0 until childSize) {
               val child = parent.getChildAt(i)
               val layoutParams = child.layoutParams as RecyclerView.LayoutParams
               val left = child.right + layoutParams.rightMargin
               val right = left + space
               if (mDivider != null) {
                    mDivider!!.setBounds(left, top, right, bottom)
                    mDivider!!.draw(canvas)
               }
               if (mPaint != null) {
                    canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mPaint)
               }
          }
     }


     //绘制横向 item 分割线
     private fun drawHorizontal(canvas: Canvas, parent: RecyclerView) {
          val left = parent.paddingLeft
          val right = parent.measuredWidth - parent.paddingRight
          val childSize = parent.childCount
          for (i in 0 until childSize) {
               val child = parent.getChildAt(i)
               val layoutParams = child.layoutParams as RecyclerView.LayoutParams
               val top = child.bottom + layoutParams.bottomMargin
               val bottom = top + space
               if (mDivider != null) {
                    mDivider!!.setBounds(left, top, right, bottom)
                    mDivider!!.draw(canvas)
               }
               if (mPaint != null) {
                    canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mPaint)
               }

          }
     }


     //绘制grideview item 分割线 不是填充满的
     private fun drawGrideview(canvas: Canvas, parent: RecyclerView) {
          val linearLayoutManager = parent.layoutManager as GridLayoutManager?
          val childSize = parent.childCount
          var other = parent.childCount / linearLayoutManager!!.spanCount - 1
          if (other < 1) {
               other = 1
          }
          other = other * linearLayoutManager.spanCount
          if (parent.childCount < linearLayoutManager.spanCount) {
               other = parent.childCount
          }
          var top: Int
          var bottom: Int
          var left: Int
          var right: Int
          var spancount: Int
          spancount = linearLayoutManager.spanCount - 1
          for (i in 0 until childSize) {
               val child = parent.getChildAt(i)
               val layoutParams = child.layoutParams as RecyclerView.LayoutParams
               if (i < other) {
                    top = child.bottom + layoutParams.bottomMargin
                    bottom = top + space
                    left = (layoutParams.leftMargin + space) * (i + 1)
                    right = child.measuredWidth * (i + 1) + left + space * i
                    if (mDivider != null) {
                         mDivider!!.setBounds(left, top, right, bottom)
                         mDivider!!.draw(canvas)
                    }
                    if (mPaint != null) {
                         canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mPaint)
                    }
               }
               if (i != spancount) {
                    top = (layoutParams.topMargin + space) * (i / linearLayoutManager.spanCount + 1)
                    bottom = (child.measuredHeight + space) * (i / linearLayoutManager.spanCount + 1) + space
                    left = child.right + layoutParams.rightMargin
                    right = left + space
                    if (mDivider != null) {
                         mDivider!!.setBounds(left, top, right, bottom)
                         mDivider!!.draw(canvas)
                    }
                    if (mPaint != null) {
                         canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mPaint)
                    }
               } else {
                    spancount += 4
               }
          }
     }

     /** */
     private fun drawGrideview1(canvas: Canvas, parent: RecyclerView) {
          val linearLayoutManager = parent.layoutManager as GridLayoutManager?
          val childSize = parent.childCount
          var top: Int
          var bottom: Int
          var left: Int
          var right: Int
          val spancount: Int
          spancount = linearLayoutManager!!.spanCount
          for (i in 0 until childSize) {
               val child = parent.getChildAt(i)
               //画横线
               val layoutParams = child.layoutParams as RecyclerView.LayoutParams
               top = child.bottom + layoutParams.bottomMargin
               bottom = top + space
               left = layoutParams.leftMargin + child.paddingLeft + space
               right = child.measuredWidth * (i + 1) + left + space * i
               if (mDivider != null) {
                    mDivider!!.setBounds(left, top, right, bottom)
                    mDivider!!.draw(canvas)
               }
               if (mPaint != null) {
                    canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mPaint)
               }
               //画竖线
               top = (layoutParams.topMargin + space) * (i / linearLayoutManager.spanCount + 1)
               bottom = (child.measuredHeight + space) * (i / linearLayoutManager.spanCount + 1) + space
               left = child.right + layoutParams.rightMargin
               right = left + space
               if (mDivider != null) {
                    mDivider!!.setBounds(left, top, right, bottom)
                    mDivider!!.draw(canvas)
               }
               if (mPaint != null) {
                    canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mPaint)
               }

               //画上缺失的线框
               if (i < spancount) {
                    top = child.top + layoutParams.topMargin
                    bottom = top + space
                    left = (layoutParams.leftMargin + space) * (i + 1)
                    right = child.measuredWidth * (i + 1) + left + space * i
                    if (mDivider != null) {
                         mDivider!!.setBounds(left, top, right, bottom)
                         mDivider!!.draw(canvas)
                    }
                    if (mPaint != null) {
                         canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mPaint)
                    }
               }
               if (i % spancount == 0) {
                    top = (layoutParams.topMargin + space) * (i / linearLayoutManager.spanCount + 1)
                    bottom = (child.measuredHeight + space) * (i / linearLayoutManager.spanCount + 1) + space
                    left = child.left + layoutParams.leftMargin
                    right = left + space
                    if (mDivider != null) {
                         mDivider!!.setBounds(left, top, right, bottom)
                         mDivider!!.draw(canvas)
                    }
                    if (mPaint != null) {
                         canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mPaint)
                    }
               }
          }
     }
}