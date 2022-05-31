package com.dexter.bluetoothlow

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.*
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.dexter.bluetoothlow.adapter.BLListAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val SCAN_PERIOD: Long = 10000

    val list: ArrayList<BluetoothDevice> = ArrayList()
    lateinit var arrayAdapter: ArrayAdapter<BluetoothDevice>
    lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var blListAdapter: BLListAdapter
    var connected : Boolean? = null

    companion object {
        val DEVICE_ADDRESS = "DEVICE_ADDRESS"
    }


    private var bluetoothService: BluetoothLeService? = null

    // Code to manage Service lifecycle.
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            componentName: ComponentName,
            service: IBinder
        ) {
            bluetoothService = (service as BluetoothLeService.LocalBinder).getService()
            bluetoothService?.let { bluetooth ->
                if (!bluetooth.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth")
                    finish()
                }
                connect(DEVICE_ADDRESS)
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothService = null
        }
    }
    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothLeService.ACTION_GATT_CONNECTED -> {
                    connected = true
//                    updateConnectionState("connected")
                }
                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    connected = false
//                    updateConnectionState("disconnected")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        blListAdapter = BLListAdapter()
        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        bluetoothAdapter =
            (this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)

        // Check to see if the BLE feature is available.

        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }
            ?.also {
                Toast.makeText(this, "Bluetooth not support", Toast.LENGTH_SHORT).show()
                finish()
            }

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Not support ", Toast.LENGTH_SHORT).show()
        }
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.ACCESS_FINE_LOCATION), 100)
            }
            startActivityForResult(enableBtIntent, 111)
        }

        btn_scan.setOnClickListener {
            scanLeDevice()
        }
        Log.d("list_check", "OnMain: ${list}")



        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = blListAdapter
        }

    }

    var scanning = false
    val handler = Handler()
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let { list.add(it.device) }
            Log.d("list_check", "onScanResult: ${list}")
            arrayAdapter.notifyDataSetChanged()
        }
    }

    // Stops scanning after 10 seconds.

    @RequiresApi(Build.VERSION_CODES.M)
    private fun scanLeDevice() {
        val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_SCAN), 101)
                }
                bluetoothLeScanner.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }

    private fun connect(address: String) {
        bluetoothAdapter.let { adapter ->
            try {
                val device = adapter.getRemoteDevice(address)
            } catch (exception: IllegalArgumentException) {
                Log.w(TAG, "Device not found with provided address.")
                return
            }
            // connect to the GATT server on the device
        } ?: run {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }

    }


    override fun onResume() {
        super.onResume()
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
        if (bluetoothService != null) {
            val result = bluetoothService!!.connect(DEVICE_ADDRESS)
            Log.d(TAG, "Connect request result=$result")
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(gattUpdateReceiver)
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter? {
        return IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
        }
    }


}