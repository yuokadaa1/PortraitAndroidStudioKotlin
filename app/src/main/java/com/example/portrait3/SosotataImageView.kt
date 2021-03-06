package com.example.portrait3

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.graphics.scale
import androidx.core.view.GestureDetectorCompat
import kotlinx.coroutines.*
import kotlin.math.abs
import kotlin.properties.Delegates

/**
 * 画像を拡大縮小、縦横斜めの慣性スロール対応カスタムイメージビュー
 */
class SosotataImageView : View, GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnDoubleTapListener {

    /** 描画用ビットマップ（表示する画像をsetImageで設定する）*/
    private lateinit var mRenderBitmap: Bitmap
    /** 拡大縮小、スクロール演算用マトリクス */
    private val mRenderMatrix: Matrix = Matrix()
    /** ジェスチャーディテクター（タッチイベントからフリック、長押し、スクロール等のイベントに変換） */
    private lateinit var mGestureDetector: GestureDetectorCompat
    /** スケールジェスチャーディテクター（タッチイベントから拡大縮小のイベントに変換） */
    private lateinit var mScaleGestureDetector: ScaleGestureDetector
    /** 縮小時の限界値（拡大は無制限）*/
    private var mScaleLimit: Float = 0f
    /** スクロール時の限界値 */
    private val mTranslateLimit: RectF = RectF()
    /** フリック操作時の慣性スクロールを制御するタスクのインスタンス */
    private val mFlingTask: FlingTask = FlingTask()


    // 画像の配列
    private var mBitmapList: List<Bitmap?> = ArrayList()
    // 画像の配列においての今のインデックス
    private var mBitmapPosition = 0
    // 画像の配列のインデックスの最小値
    private val mPositionMin = 0
    // 画像の配列のインデックスの最小値
    private var mPositionMax = 0
    // 画像切り替え後、初めてのonDrawかどうか
    private var isChangedFirstDraw = true
    // 表示している画像の縮尺
    private var mLastScaleFactor = 1.0f

    var vwWidth by Delegates.notNull<Float>()    //ViewWidgetの横幅
    var vwHeight by Delegates.notNull<Float>() //ViewWidgetの縦幅


    /**
     * フリック操作時の慣性スクロールを制御するタスク
     */
    inner class FlingTask {

        /** 慣性スクロール制御コルーチン */
        private lateinit var flingCoroutine: FlingCoroutine

        /**
         * フリック時の慣性スクロールを実行する
         * @param velocityX 横方向の加速度（フリックの強さとして使用する）
         * @param velocityY 縦方向の加速度（フリックの強さとして使用する）
         * @param restart 実行中に再スタートする
         * */
        fun fling(velocityX: Float, velocityY: Float, restart: Boolean) {
            Log.i("FlingTask","の起動")
            if (::flingCoroutine.isInitialized) {
                if (!restart && flingCoroutine.isFling) {
                    return
                }
                flingCoroutine.cleanUp()
            }
            flingCoroutine = FlingCoroutine()
            flingCoroutine.fling(velocityX, velocityY)
        }

        /**
         * 慣性スクロール制御コルーチン
         */
        inner class FlingCoroutine {

            /** スコープ */
            private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

            var isFling: Boolean = false

            /**
             * フリック時の慣性スクロールを実行する
             * @param velocityX 横方向の加速度（フリックの強さとして使用する）
             * @param velocityY 縦方向の加速度（フリックの強さとして使用する）
             * */
            fun fling(velocityX: Float, velocityY: Float) {
                Log.i("FlingCoroutine","の起動")
                isFling = true

                scope.launch {
                    val mtr = FloatArray(9)
                    var vx = velocityX / 60     // 初速度を少し減らす（感覚での調整）
                    var vy = velocityY / 60     // 初速度を少し減らす（感覚での調整）
                    var isOverLLimit = false    // 左座標の限界値を超えた
                    var isOverRLimit = false    // 右座標の限界値を超えた
                    var isOverTLimit = false    // 上座標の限界値を超えた
                    var isOverBLimit = false    // 座標の限界値を超えた
                    var isBeforeTurnBackX = false   // 跳ね返り前フラグ
                    var isBeforeTurnBackY = false   // 跳ね返り前フラグ
                    var stopX = 0.05f            // 移動距離がこの値より小さくなったら止める
                    var stopY = 0.05f            // 移動距離がこの値より小さくなったら止める

                    //
                    // 慣性スクロールループ
                    //
                    while (isActive) {
                        Log.i("FlingCoroutine","慣性スクロールループの起動")
                        translate(vx, vy)
                        mRenderMatrix.getValues(mtr)

                        //
                        // 上下左右の限界値超えチェック
                        //
                        if (!isOverRLimit) {
                            if (mTranslateLimit.left < mtr[Matrix.MTRANS_X]) {
                                if (!isOverLLimit && !isBeforeTurnBackX) {
                                    // 左座標の限界値を超えた
                                    isOverLLimit = true
                                    isBeforeTurnBackX = true
                                }
                            }
                        }
                        if (!isOverLLimit) {
                            if (mtr[Matrix.MTRANS_X] < mTranslateLimit.right) {
                                if (!isOverLLimit && !isOverRLimit && !isBeforeTurnBackX) {
                                    // 右座標の限界値を超えた
                                    isOverRLimit = true
                                    isBeforeTurnBackX = true
                                }
                            }
                        }
                        if (!isOverBLimit) {
                            if (mTranslateLimit.top < mtr[Matrix.MTRANS_Y]) {
                                if (!isOverTLimit && !isBeforeTurnBackY) {
                                    // 上座標の限界値を超えた
                                    isOverTLimit = true
                                    isBeforeTurnBackY = true
                                }
                            }
                        }
                        if (!isOverTLimit) {
                            if (mtr[Matrix.MTRANS_Y] < mTranslateLimit.bottom) {
                                if (!isOverTLimit && !isOverBLimit && !isBeforeTurnBackY) {
                                    // 下座標の限界値を超えた
                                    isOverBLimit = true
                                    isBeforeTurnBackY = true
                                }
                            }
                        }

                        //
                        // 移動距離を減らしていく
                        // ・移動限界値以内：x0.8f
                        // ・移動限界値以上（跳ね返り前）：x0.4f
                        // ・移動限界値以上（跳ね返り後）：x0.9f
                        //
                        vx *= if (isOverLLimit || isOverRLimit) {
                            if (isBeforeTurnBackX) {
                                0.4f
                            } else {
                                0.9f
                            }
                        } else {
                            0.8f
                        }
                        vy *= if (isOverTLimit || isOverBLimit) {
                            if (isBeforeTurnBackY) {
                                0.4f
                            } else {
                                0.9f
                            }
                        } else {
                            0.8f
                        }

                        //
                        // 慣性スクロール停止判定
                        // 上下左右の限界値を超えていたら跳ね返りを表現するために移動距離を再設定する
                        //
                        if (abs(vx) < stopX) {
                            if ((isOverLLimit || isOverRLimit) && isBeforeTurnBackX) {
                                // 跳ね返りのため移動距離を再設定
                                vx = if (mTranslateLimit.right > 0) {
                                    mtr[Matrix.MTRANS_X] - mTranslateLimit.left
                                } else {
                                    if (isOverLLimit) {
                                        mtr[Matrix.MTRANS_X] - mTranslateLimit.left
                                    } else {
                                        mtr[Matrix.MTRANS_X] - mTranslateLimit.left - mTranslateLimit.right
                                    }
                                }
                                vx *= -0.1f
                                isBeforeTurnBackX = false
                            } else {
                                // 左右座標の限界を超えずに終了
                                vx = 0f
                            }
                        }
                        if (abs(vy) < stopY) {
                            if ((isOverTLimit || isOverBLimit) && isBeforeTurnBackY) {
                                // 跳ね返りのため移動距離を再設定
                                vy = if (mTranslateLimit.bottom > 0) {
                                    mtr[Matrix.MTRANS_Y] - mTranslateLimit.top
                                } else {
                                    if (isOverTLimit) {
                                        mtr[Matrix.MTRANS_Y] - mTranslateLimit.top
                                    } else {
                                        mtr[Matrix.MTRANS_Y] - mTranslateLimit.top - mTranslateLimit.bottom
                                    }
                                }
                                vy *= -0.1f
                                isBeforeTurnBackY = false
                            } else {
                                // 左右
                                //上下座標の限界を超えずに終了
                                vy = 0f
                            }
                        }
                        if (vx == 0f && vy == 0f) {
                            // 縦横両方とも停止の閾値内に入った時点で終了
                            break
                        }
                        Thread.sleep(16) // 短くした方が滑らか
                    }
                    isFling = false
                }
            }

            fun cleanUp() {
                scope.cancel()
            }
        }
    }

    /**
     * コンストラクタ
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    //constructor追加してみる？何だろうね？
    constructor(context: Context) : super(context) {
        //setContentViewでonDrawを起動してしまうのでここで自動起動をoffにする。
        this.setWillNotDraw(true)
    }

    /**
     * 描画用ビットマップを設定する
     */
    fun setImage(bmp: Bitmap) {
        Log.i("setImage","の起動")
        mRenderBitmap = bmp
        updateScaleLimit()
        updateTranslateLimit()
        renderBitmap()//ここでonDrawを呼んでいる
       // なにかよくわからんけどここを有効にするとonDraw（などなど）が呼び出されまくる
       mFlingTask.fling(0F, 0F, true)
    }

    fun setBitmapList(bitmapList: List<Bitmap?>) {

        Log.i("setBitmapList", "起動確認")

        //ViewWidgetのサイズを取得
        vwWidth = this.width.toFloat()
        vwHeight = this.height.toFloat()

        // 外部から画像の配列を取り入れる。
        this.setWillNotDraw(false)
        this.mBitmapList = bitmapList
        // 最大値を初期化
        this.mPositionMax = bitmapList.size - 1
        this.setImage(mBitmapList[mBitmapPosition]!!)

    }

    /**
     * 描画を更新する
     */
    fun renderBitmap() {
        Log.i("renderBitmap","の起動")
        invalidate()
    }

    /**
     * [android.view.View.onSizeChanged]
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        Log.i("onSizeChanged","の起動")
        super.onSizeChanged(w, h, oldw, oldh)

        if (w > 0 && h > 0) {
            if (!::mGestureDetector.isInitialized) {
                mGestureDetector = GestureDetectorCompat(context, this)
            }
            if (!::mScaleGestureDetector.isInitialized) {
                mScaleGestureDetector = ScaleGestureDetector(context, this)
            }
            updateScaleLimit()
            updateTranslateLimit()
            renderBitmap()
            mFlingTask.fling(0F, 0F, true)
        }
    }


    /**
     * [android.view.View.onDraw]
     */
    override fun onDraw(canvas: Canvas) {
        Log.i("onDraw","onDrawの起動")
        super.onDraw(canvas)

        if(isChangedFirstDraw == true){

            // 画面の横幅に画像の横を合わせるには何倍すればいいか
            val scaleX = vwWidth / (mBitmapList[mBitmapPosition]!!.width * mScaleLimit)
            // val scaleX = vwWidth / mBitmapList[mBitmapPosition]!!.width * Matrix.MSCALE_X
            // 画面の横幅に画像の横を合わせるには何倍すればいいか
            val scaleY = vwHeight / (mBitmapList[mBitmapPosition]!!.height * mScaleLimit)
            // 小さいほうを適応させる
            mLastScaleFactor = Math.min(scaleX, scaleY)
            //画像を画面中心に配置する。
            // mRenderMatrix.postScale(mLastScaleFactor, mLastScaleFactor, 0f,0f)

            //どうも、onDraw以外でのmRenderMatrix編集で位置はずれてしまっている模様
            val dx = Math.round((vwWidth - mBitmapList[mBitmapPosition]!!.width * mScaleLimit) * 0.5f);
            val dy = Math.round((vwHeight - mBitmapList[mBitmapPosition]!!.height * mScaleLimit) * 0.5f);
            //Matrix.setXXは他のparmを初期化するのでsetTran->setScaleはsetTranが初期化される
            mRenderMatrix.setScale(mScaleLimit, mScaleLimit)
            mRenderMatrix.postTranslate(dx.toFloat(), dy.toFloat())


            Log.i("onDraw","mScaleLimit:" + mScaleLimit)
            Log.i("onDraw","dx:" + dx)
            Log.i("onDraw","dy:" + dy)


        }

        if (::mRenderBitmap.isInitialized) {
            Log.i("onDraw","canvas.drawBitmapの起動")

            val values3 = FloatArray(9)
            mRenderMatrix.getValues(values3)
            Log.i("onDraw", "valuesX" + values3[0].toString())
            Log.i("onDraw", "valuesY" + values3[4].toString())
            Log.i("onDraw", "valuesXpo" + values3[2].toString())
            Log.i("onDraw", "valuesYpo" + values3[5].toString())

            canvas.drawBitmap(mRenderBitmap, mRenderMatrix, null)
        }

    }

    /**
     * [android.view.View.onTouchEvent]
     */
    override fun onTouchEvent(e: MotionEvent): Boolean {

        Log.i("onTouchEvent","の起動")

        mGestureDetector.onTouchEvent(e)
        mScaleGestureDetector.onTouchEvent(e)

        if (e.action == MotionEvent.ACTION_UP || e.action == MotionEvent.ACTION_CANCEL) {
            // はみ出したままになるケースの回避策
            // ・ACTION_UP：スクロールや拡大縮小の完了時
            // ・ACTION_CANCEL：２本指で操作中に３本指を追加するとACTION_CANCEL発生後にタッチイベントが止まる
            mFlingTask.fling(0F, 0F, false)
        }

        return true
    }

    /**
     * [android.view.GestureDetector.OnGestureListener.onShowPress]
     */
    override fun onShowPress(e: MotionEvent?) {
    }

    /**
     * [android.view.GestureDetector.OnGestureListener.onSingleTapUp]
     */
    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        Log.i("onSingleTapUp","の起動")
        return true
    }

    /**
     * [android.view.GestureDetector.OnGestureListener.onDown]
     */
    override fun onDown(e: MotionEvent?): Boolean {
        Log.i("onDown","の起動")
        return true
    }

    /**
     * [android.view.GestureDetector.OnGestureListener.onFling]
     */
    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        Log.i("onFling","の起動")
        mFlingTask.fling(velocityX, velocityY, true)
        return true
    }

    /**
     * [android.view.GestureDetector.OnGestureListener.onScroll]
     */
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {

        Log.i("onScroll","の起動")

        //onDrawはflingのwhile(activ)で何回も起動するので、画像サイズの変更有無で初回Drawを判定する
        isChangedFirstDraw = false

        var dx: Float = -distanceX
        var dy: Float = -distanceY

        // 限界を超えたときに移動距離を縮めて(x0.3)引っ張っている感覚を表現する
        val mtr = FloatArray(9)
        mRenderMatrix.getValues(mtr)
        if (mTranslateLimit.left < mtr[Matrix.MTRANS_X]) {
            dx *= 0.3F
        } else if (mtr[Matrix.MTRANS_X] < mTranslateLimit.right) {
            dx *= 0.3F
        }

        if (mTranslateLimit.top < mtr[Matrix.MTRANS_Y]) {
            dy *= 0.3F
        } else if (mtr[Matrix.MTRANS_Y] < mTranslateLimit.bottom) {
            dy *= 0.3F
        }

        translate(dx, dy)
        return true
    }

    /**
     * [android.view.GestureDetector.OnGestureListener.onLongPress]
     */
    override fun onLongPress(e: MotionEvent?) {
        Log.i("onLongPress","の起動")
    }

    /**
     * [android.view.ScaleGestureDetector.OnScaleGestureListener.onScaleBegin]
     */
    override fun onScaleBegin(e: ScaleGestureDetector?): Boolean {
        Log.i("onScaleBegin","の起動")
        return true
    }

    /**
     * [android.view.ScaleGestureDetector.OnScaleGestureListener.onScaleEnd]
     */
    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        Log.i("onScaleEnd","の起動")
    }

    /**
     * [android.view.ScaleGestureDetector.OnScaleGestureListener.onScale]
     */
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        Log.i("onScale","の起動")
        val scale = detector.currentSpan / detector.previousSpan
        val values = FloatArray(9)
        mRenderMatrix.getValues(values)
        if (values[Matrix.MSCALE_X] * scale <= mScaleLimit) {
            // 縮小限界値で止める。ここが限界まで縮小したときの処理（初期表示にしたい）
            values[Matrix.MSCALE_X] = mScaleLimit
            values[Matrix.MSCALE_Y] = mScaleLimit
            mRenderMatrix.setValues(values)
            Log.i("onScale","mRenderMatrix" + mRenderMatrix.toString())
        } else {
            mRenderMatrix.postScale(scale, scale, detector.focusX, detector.focusY)
        }
        updateTranslateLimit()
        renderBitmap()
        return true
    }

    /**
     * 縮小限界値を更新する
     */
    private fun updateScaleLimit() {
        Log.i("updateScaleLimit","の起動")
        if (::mRenderBitmap.isInitialized) {
            val widthLimit: Float = this.width.toFloat() / mRenderBitmap.width.toFloat()
            val heightLimit: Float = this.height.toFloat() / mRenderBitmap.height.toFloat()

            // 縮小限界値を画像の縦、横で短い方が画面サイズと一致するまでにする。最大は無限。
            mScaleLimit = if (widthLimit < heightLimit) widthLimit else heightLimit
            Log.i("updateScaleLimit","mScaleLimit:" + mScaleLimit)
        }
    }

    /**
     * 画像を移動する
     * @param distanceX 横方向の移動距離
     * @param distanceY 縦方向の移動距離
     */
    private fun translate(distanceX: Float, distanceY: Float) {
        Log.i("translate","の起動")
        val values = FloatArray(9)
        mRenderMatrix.getValues(values)
        if (values[Matrix.MSCALE_X] < mScaleLimit) {
            // 移動時に縮小限界値を超えている場合は、移動をキャンセルして縮小限界値(横幅いっぱい)に補正する。
            // 最小表示の状態で画面回転すると表示領域サイズが変わって縮小限界値を超えてしまうため。
            mRenderMatrix.setScale(mScaleLimit, mScaleLimit);
            updateTranslateLimit();
        } else {
            mRenderMatrix.postTranslate(distanceX, distanceY)
        }
        renderBitmap()
    }

    /**
     * 移動限界値を更新する
     */
    private fun updateTranslateLimit() {
        Log.i("updateTranslateLimit","の起動")
        if (::mRenderBitmap.isInitialized) {
            val values = FloatArray(9)
            mRenderMatrix.getValues(values)

            val scaleWidth = mRenderBitmap.width.toFloat() * values[Matrix.MSCALE_X]    //画像の横幅
            val scaleHeight = mRenderBitmap.height.toFloat() * values[Matrix.MSCALE_Y]  //画像の縦幅

            if (scaleWidth < vwWidth) {
                // 縮小で画面サイズを超える場合、画像が中央寄せで左右に余白ができるよう限界値を調整する
                mTranslateLimit.left = (vwWidth - scaleWidth) / 2
                mTranslateLimit.right = vwWidth - mTranslateLimit.left
            } else {
                mTranslateLimit.left = 0f
                mTranslateLimit.right = vwWidth - scaleWidth
            }
            if (scaleHeight < vwHeight) {
                // 縮小で画面サイズを超える場合、画像が中央寄せで上下に余白ができるよう限界値を調整する
                mTranslateLimit.top = (vwHeight - scaleHeight) / 2
                mTranslateLimit.bottom = vwHeight - mTranslateLimit.top
            } else {
                mTranslateLimit.top = 0f
                mTranslateLimit.bottom = vwHeight - scaleHeight
            }
        }
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        return true
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {

        Log.i("onDoubleTap", "DoubleClickされました:" + mBitmapPosition)


        //画像が最後尾に行った状態でダブルクリックされた場合は最初に戻す
        // if(mPositionMax != 0){mBitmapPosition++}
        // if(mBitmapPosition > mPositionMax){
        //     mBitmapPosition = mPositionMin
        // }

        if (e!!.x > this.width / 2) {
            // 画面右側だったら画像を一個進める
            mBitmapPosition++
            // 限界を超えたら一番前に戻る
            if (mBitmapPosition > mPositionMax) {
                mBitmapPosition = mPositionMin
            }
        } else {
            // 画面左側だったら画像を一個戻す
            mBitmapPosition--
            // 限界を下回ったら一番後ろにいく
            if (mBitmapPosition < mPositionMin) {
                mBitmapPosition = mPositionMax
            }
        }

        isChangedFirstDraw = true

        this.setImage(mBitmapList[mBitmapPosition]!!)
        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        return true
    }
}