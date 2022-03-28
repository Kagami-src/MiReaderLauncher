package com.kagami.mireaderlauncher

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.kagami.mireaderlauncher.databinding.ActivityMainBinding
import com.kagami.mireaderlauncher.databinding.ItemAppBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: AppsViewModel by viewModels()
    lateinit var adapter:Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter=Adapter()
        binding.recyclerView.adapter=adapter
        buildNotification()
        viewModel.appsLiveData.observe(this, Observer {
            adapter.dataSet=it
        })
    }

    override fun onStart() {
        super.onStart()
        viewModel.reloadApps()
    }
    fun uninstallApp(appId:String){
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.parse("package:$appId")
        startActivity(intent)
    }


    fun startAppDetail(packageName: String){
        val intent=Intent()
        intent.action=Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS
        //val uri=Uri.fromParts("package",packageName,null)
        //intent.setData(uri)
        startActivity(intent)
    }
    fun startActivity(packageName: String) {
        var intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) { // We found the activity now start the activity
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {

        }
    }
    private fun buildNotification() {
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = "mainchannel"
            val descriptionText = "mainchannel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("mainchannel", name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
        val resultPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val mNotificationBuilder = NotificationCompat.Builder(this,"mainchannel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setColor(getColor(R.color.colorPrimary))
            .setContentTitle(getString(R.string.app_name))
            .setOngoing(true)
            .setContentIntent(resultPendingIntent)
            with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(1, mNotificationBuilder.build())
        }
        //startForeground(FOREGROUND_SERVICE_ID, mNotificationBuilder.build())
    }


    inner class Adapter:RecyclerView.Adapter<ViewHolder>(){
        var dataSet = listOf<AppsViewModel.AppData>()
         set(value) {
             field=value
             notifyDataSetChanged()
         }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)).apply {
                itemView.setOnClickListener {
                    startActivity(data.appId)
                    //startAppDetail(data.appId)
                }
                itemView.setOnLongClickListener {
                    uninstallApp(data.appId)
                    true
                }
            }
        }

        override fun getItemCount(): Int {
            return dataSet.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data=dataSet[position]
            holder.data=data
            with(holder.binding) {
                nameText.text=data.name
                if(data.icon!=null){
                    imageView.setImageDrawable(data.icon)
                }else{
                    imageView.setImageResource(R.mipmap.ic_launcher)
                }
            }
        }

    }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemAppBinding.bind(view)
        lateinit var data:AppsViewModel.AppData
    }



}
