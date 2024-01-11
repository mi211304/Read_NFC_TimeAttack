package com.example.nfctest4

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.nfc.tech.NfcF
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.*


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener{

    private var timerText: TextView? = null                          //カウントアップ、ダウンの初期化
    private var timerText2: TextView? = null
    private val dataFormat = SimpleDateFormat("mm:ss.S", Locale.US)
    private val dataFormat2 = SimpleDateFormat("ss", Locale.US)
    private var count = 0
    private var period = 0
    private var count2 = 1
    private var period2 = 0

    private var textToSpeech: TextToSpeech? = null                   //ttsの初期化

    private var seigo: TextView? = null                              //テキスト系の初期化
    private var tvMain: TextView? = null
    private var resetText: TextView? = null


    private var ans: String? = null
    private var nextNumber: String? = null

    private var nfcAdapter: NfcAdapter? = null


    private val handler = Handler(Looper.getMainLooper())           //カウントアップ処理
    private val up: Runnable = object : Runnable {
        override fun run() {
            count++
            timerText!!.text = dataFormat.format(count * period)
            handler.postDelayed(this, period.toLong())
            handler2.removeCallbacks(down)
        }
    }

    private val handler2 = Handler(Looper.getMainLooper())          //カウントダウン処理
    private val down: Runnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            if (count2 != 0) {
                count2--
                timerText2!!.text = dataFormat2.format(count2 * period2)
                handler2.postDelayed(this, period2.toLong())
            }
            else {                                                   //カウントが０になったらリスタート処理
                resetText!!.text = ""                                //画面の初期化
                timerText2!!.text = ""
                seigo!!.text = "正誤判定"
                tvMain!!.text = "nothing"
                count = 0                                            //カウントアップタイマーの初期化
                period = 100
                timerText!!.text = dataFormat.format(count * period)
                getNextRandomNumber()
                ans = MyList.originalList[nextNumber?.toInt() ?: 0]
                if (ans != null) {
                    //val textToRead = ans.toString() // 読み上げたいテキストを設定してください
                    //startSpeak(textToRead, true) // テキストを読み上げ
                    startSpeak(ans!!, true)
                }
                else {

                }
                handler.post(up)                                     //カウントアップスタート
                felica.start()                                       //nfcリーダースタート
            }
        }
    }
    //val reset_text2 = findViewById<TextView>(R.id.reset_text2)

    private fun startSpeak(text: String, isImmediately: Boolean){

        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
            }

            override fun onError(utteranceId: String) {

            }

            override fun onStart(utteranceId: String) {

            }
        })
    }

    private fun getNextRandomNumber() {
        nextNumber = Numbers.getNextNumber().toString()

    }



    val felica = FelicaReader(this, this)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        felica.setListener(felicaListener)

        count = 0
        period = 100
        timerText = findViewById(R.id.timer)
        timerText?.run { text = dataFormat.format(0) }
        seigo = findViewById(R.id.seigo)
        tvMain = findViewById(R.id.tvMain)
        textToSpeech = TextToSpeech(this,this)

        // カウントスタート
        val startButton = findViewById<Button>(R.id.start_button)
        startButton.setOnClickListener {
            getNextRandomNumber()
            ans = MyList.originalList[nextNumber?.toInt() ?: 0]
            if (ans != null) {
                //val textToRead = ans.toString() // 読み上げたいテキストを設定してください
                //startSpeak(textToRead, true) // テキストを読み上げ
                startSpeak(ans!!, true)
            }
            else {

            }
            handler.post(up)
        }


        // タイマー強制終了
        val stopButton = findViewById<Button>(R.id.stop_button)
        stopButton.setOnClickListener {
            handler.removeCallbacks(up)
        }

        //　タイマー強制リセット
        val resetButton = findViewById<Button>(R.id.reset_button)
        resetButton.setOnClickListener {
            seigo!!.text = "正誤判定"                               //画面の初期化
            tvMain!!.text = "nothing"
            count = 0                                             //カウントアップタイマーの初期化
            period = 100
            timerText!!.text = dataFormat.format(count * period)
        }
    }

    override fun onResume() {
        super.onResume()
        felica.start()                                            //nfcリーダースタート
    }

    override fun onPause() {
        super.onPause()
        felica.stop()                                             //nfcリーダーストップ
    }



    private val felicaListener = object : FelicaReaderInterface{

        override fun onReadTag(tag : Tag) {                      // データ受信イベント

            val idm : ByteArray = tag.id
            val nfcData = byteToHex(idm)
            //val reset_text = findViewById<TextView>(R.id.reset_print)

            tag.techList
            tvMain!!.text = byteToHex(idm)
            seigo = findViewById(R.id.seigo)

            felica.stop()                                       //nfcリーダーストップ
            handler.removeCallbacks(up)                         //カウントストップ

            if (ans != null && ans == nfcData) {
                val textToRead = "正解"                          // 読み上げたいテキストを設定してください
                startSpeak(textToRead, true)
                seigo!!.text = "正解"
            } else {
                val textToRead = "間違い"                         // 読み上げたいテキストを設定してください
                startSpeak(textToRead, true)
                seigo!!.text = "間違い"
            }


            count2 = 60                                             //リスタート処理
            period2 = 100
            timerText2 = findViewById(R.id.reset_text2)
            timerText2?.run { text = dataFormat2.format(0) }
            resetText = findViewById(R.id.reset_print)

            resetText!!.text = "リスタートまで"
            handler2.post(down)
            timerText2!!.text = dataFormat2.format(count2 * period2)


            Log.d("Sample", byteToHex(idm))
        }

        override fun onConnect() {

            Log.d("Sample","onConnected")
        }
    }

    private fun byteToHex(b : ByteArray) : String{
        var s = ""
        for (i in 0 until b.size){
            s += "[%02X]".format(b[i])
        }
        return s
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.let { tts ->
                val locale = Locale.JAPAN
                if (tts.isLanguageAvailable(locale) > TextToSpeech.LANG_AVAILABLE) {
                    tts.language = Locale.JAPAN
                } else {
                    // 言語の設定に失敗
                }
            }


        } else {
            // Tts init 失敗
        }
    }
}


interface FelicaReaderInterface : FelicaReader.Listener {
    fun onReadTag(tag : Tag)                        // タグ受信イベント
    fun onConnect()
}

class FelicaReader(private val context: Context, private val activity : Activity) : Handler() {
    private var nfcmanager : NfcManager? = null
    private var nfcadapter : NfcAdapter? = null
    private var callback : CustomReaderCallback? = null
    private var listener: FelicaReaderInterface? = null
    interface Listener {}


    fun start(){
        callback = CustomReaderCallback()
        callback?.setHandler(this)
        nfcmanager = context.getSystemService(Context.NFC_SERVICE) as NfcManager?
        nfcadapter = nfcmanager!!.defaultAdapter
        nfcadapter!!.enableReaderMode(activity,callback
            ,  NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_V or
                    NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,null)
    }
    fun stop(){
        nfcadapter!!.disableReaderMode(activity)
        callback = null
    }

    override fun handleMessage(msg: Message) {                  // コールバックからのメッセージクラス
        if (msg.arg1 == 1){                                     // 読み取り終了
            listener?.onReadTag(msg.obj as Tag)                 // 拡張用
        }
        if (msg.arg1 == 2){                                     // 読み取り終了
            listener?.onConnect()                               // 拡張用
        }
    }

    fun setListener(listener: Listener?) {                      // イベント受け取り先を設定
        if (listener is FelicaReaderInterface) {
            this.listener = listener
        }
    }

    private class CustomReaderCallback : NfcAdapter.ReaderCallback {
        private var handler : Handler? = null
        override fun onTagDiscovered(tag: Tag) {
            Log.d("Sample", tag.id.toString())
            val msg = Message.obtain()
            msg.arg1 = 1
            msg.obj = tag
            if (handler != null) handler?.sendMessage(msg)
            val nfc : NfcF = NfcF.get(tag) ?: return
            try {
                nfc.connect()
                nfc.close()
                msg.arg1 = 2
                msg.obj = tag
                if (handler != null) handler?.sendMessage(msg)
            }catch (e : Exception){
                nfc.close()
            }
        }
        fun setHandler(handler  : Handler){
            this.handler = handler
        }
    }
}
