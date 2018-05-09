package com.example.matt.trivialthisfinal

import android.app.ActionBar
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import org.jetbrains.anko.toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class MainActivity : AppCompatActivity() {

    var myUUID: UUID = UUID.fromString("1c463fe7-09d1-4d5c-bdd0-0722931b4f6a")

    private val TAG = "MainActivity"

    // Intent request codes
    private val REQUEST_CONNECT_DEVICE_SECURE = 1
    private val REQUEST_CONNECT_DEVICE_INSECURE = 2
    private val REQUEST_ENABLE_BT = 3

    /**
     * Name of the connected device
     */
    private var mConnectedDeviceName: String? = null

    /**
     * Array adapter for the conversation thread
     */
    private var mConversationArrayAdapter: ArrayAdapter<String>? = null

    /**
     * String buffer for outgoing messages
     */
    private val mOutStringBuffer: StringBuffer? = null

    /**
     * Local Bluetooth adapter
     */
    private var mBluetoothAdapter: BluetoothAdapter? = null

    /**
     * Member object for the chat services
     */
    private var mChatService: BluetoothChatService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setHasOptionsMenu(true)
        setContentView(R.layout.activity_main)
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            val activity = this
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show()
            activity.finish()
        }

        play_btn.isEnabled = false
        player1_toggle?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
//                play_btn.visibility = View.VISIBLE
                play_btn.isEnabled = true
                player2_toggle.isEnabled = false
                val serverIntent = Intent(this, DeviceListActivity::class.java)
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE)

            } else {
//                play_btn.visibility = View.INVISIBLE
                play_btn.isEnabled = false
                player2_toggle.isEnabled = true
            }
        }
        player2_toggle?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
//                play_btn.visibility = View.VISIBLE
                player1_toggle.isEnabled = false
                play_btn.isEnabled = true

            } else {
                play_btn.isEnabled = false
//                play_btn.visibility = View.INVISIBLE
                player1_toggle.isEnabled = true
            }
        }

        play_btn.setOnClickListener {
            when {
                player1_toggle.isChecked -> {
                    gameScreen(1)
                }
                player2_toggle.isChecked -> {
                    gameScreen(2)

//                    val play2Intent = Intent(this, Player2_Hinter::class.java)
//                    this.startActivity(play2Intent)
                }
                else -> toast("Choose a Player!")
            }
        }

//        discoverable_btn.setOnClickListener{
//            ensureDiscoverable()
//        }
    }
    private fun gameScreen(player:Int) {
        if(player == 1){
            Guesser.visibility = View.VISIBLE
            play_btn.visibility = View.GONE
            player1_toggle.visibility = View.GONE
            player2_toggle.visibility = View.GONE

        }else if (player == 2){
            Hinter.visibility = View.VISIBLE
            play_btn.visibility = View.GONE
            player1_toggle.visibility = View.GONE
            player2_toggle.visibility = View.GONE
        } else{
            Guesser.visibility = View.GONE
            Hinter.visibility = View.GONE
            play_btn.visibility = View.VISIBLE
            player1_toggle.visibility = View.VISIBLE
            player2_toggle.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        gameScreen(3)
//        if(isHome())
//            super.onBackPressed()

    }
    private fun isHome():Boolean{

        return (Guesser.visibility == View.GONE && Hinter.visibility == View.GONE
                &&  play_btn.visibility == View.VISIBLE &&
                player1_toggle.visibility == View.VISIBLE &&
                player2_toggle.visibility == View.VISIBLE)
    }


    public override fun onStart() {
        super.onStart()
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        mChatService?.stop()
    }

    public override fun onResume() {
        super.onResume()

        Log.i(TAG,"onResume")
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService!!.state == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService!!.start()
            }
        }
    }
    private fun setupChat() {
        Log.d(TAG, "setupChat()")

        mChatService = BluetoothChatService(this, mHandler)


    }

    private fun connectDevice(data: Intent, secure: Boolean) {
        // Get the device MAC address
        val address = data.extras!!
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS)
        // Get the BluetoothDevice object
        val device = mBluetoothAdapter!!.getRemoteDevice(address)
        // Attempt to connect to the device
        mChatService!!.connect(device, secure)
    }


    fun sendMessage(message: String) {
        // Check that we're actually connected before trying anything
        if (mChatService!!.state != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show()
            Log.i(TAG,"sending message hi")
            //send message from here - but using what
            return
        }

        // Check that there's actually something to send
        if (message.isNotEmpty()) {
            // Get the message bytes and tell the BluetoothChatService to write
            val send = message.toByteArray()
            mChatService!!.write(send)

            // Reset out string buffer to zero and clear the edit text field

        }
    }

    private fun ensureDiscoverable() {
        if (mBluetoothAdapter!!.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            startActivity(discoverableIntent)
        }
    }

    fun setStatus( resId:Int) {
        val activity: FragmentActivity = this ?: return
        val actionBar: ActionBar = activity.actionBar ?: return
        actionBar.setSubtitle(resId)
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    fun setStatus( subTitle:CharSequence) {
        val activity: MainActivity = this ?: return
        val  actionBar : ActionBar = activity.actionBar ?: return
        actionBar.subtitle = subTitle
    }


    private var mHandler = Handler{
        when (it.what) {
            Constants.MESSAGE_STATE_CHANGE -> {
                val status = when (it.arg1) {
                    BluetoothChatService.STATE_CONNECTED -> ""
                    BluetoothChatService.STATE_CONNECTING -> ""
                    BluetoothChatService.STATE_LISTEN -> ""
                    BluetoothChatService.STATE_NONE -> ""
                    else -> ""
                }
                Log.i(TAG,"state change $status")
                if(it.arg1 == BluetoothChatService.STATE_CONNECTED)
                    Log.i(TAG,"sending message hi")
                sendMessage("hi")
            }
            Constants.MESSAGE_WRITE -> {
                val writeBuf = it.obj as ByteArray
                // construct a string from the buffer
                val writeMessage = String(writeBuf)
                Log.i(TAG,"sent $writeMessage")
            }
            Constants.MESSAGE_READ -> {
                val readBuf = it.obj as ByteArray
                // construct a string from the valid bytes in the buffer
                val readMessage = String(readBuf, 0, it.arg1)
                Log.i(TAG, "message read - $readMessage")
                //read message from here
            }
            Constants.MESSAGE_DEVICE_NAME -> {
                // save the connected device's name
                mConnectedDeviceName = it.data.getString(Constants.DEVICE_NAME)
                Toast.makeText(this, "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show()
            }
            Constants.MESSAGE_TOAST -> {
                Toast.makeText(this, it.data.getString(Constants.TOAST), Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
        true
    }





    private val mWriteListener = object : TextView.OnEditorActionListener {
        override fun onEditorAction(view: TextView, actionId: Int, event: KeyEvent): Boolean {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.action === KeyEvent.ACTION_UP) {
                val message = view.text.toString()
                sendMessage(message)
            }
            return true
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        public void onActivityResult(int requestCode, int resultCode, Intent data) {
        when (requestCode) {
            REQUEST_CONNECT_DEVICE_SECURE ->
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG,"Calling connectdevice")
                    connectDevice(data!!, true)
                }

            REQUEST_CONNECT_DEVICE_INSECURE ->
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG,"Calling connectdevice")
                    connectDevice(data!!, false)
                }

            REQUEST_ENABLE_BT ->
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat()
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled")
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }
}
