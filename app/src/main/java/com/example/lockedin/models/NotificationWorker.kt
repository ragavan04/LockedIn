import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.lockedin.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val communityName = inputData.getString("communityName") ?: "Community"
        val title = inputData.getString("title") ?: "Notification"
        val message = inputData.getString("message") ?: "Time to post in your community!"

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, "channel_id")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(communityName.hashCode(), notification)

        return Result.success()
    }
}


fun calculateNotificationDelay(notificationTime: String): Long {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val currentTime = Calendar.getInstance()

    // Parse the notification time
    val targetTime = Calendar.getInstance().apply {
        val parsedTime = timeFormat.parse(notificationTime) ?: return -1L
        time = parsedTime
        set(Calendar.YEAR, currentTime.get(Calendar.YEAR))
        set(Calendar.MONTH, currentTime.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, currentTime.get(Calendar.DAY_OF_MONTH))
    }

    // If the target time has already passed today, schedule it for tomorrow
    if (targetTime.before(currentTime)) {
        targetTime.add(Calendar.DAY_OF_MONTH, 1)
    }

    return targetTime.timeInMillis - currentTime.timeInMillis
}

fun scheduleCommunityNotification(context: Context, delayMillis: Long, communityName: String) {
    if (delayMillis > 0) {
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                Data.Builder()
                    .putString("communityName", communityName)
                    .putString("title", "Time to Post!")
                    .putString("message", "It's time to upload your post for the $communityName community.")
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
