package com.baosystems.icrc.pharmacystockmanagement.data.repositories

import com.baosystems.icrc.pharmacystockmanagement.data.TransactionType
import com.baosystems.icrc.pharmacystockmanagement.data.models.UserActivity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UserActivityRepository {
    val activities = ArrayList<UserActivity>()

    init {
        activities.addAll(getSampleData())
    }

    fun getSampleData(): List<UserActivity> {
        val sampleDestinations = DestinationRepository().getSampleDestinations()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        return listOf(
            UserActivity(
                TransactionType.DISTRIBUTION,
                LocalDateTime.parse("2021-06-28 10:00:02", formatter),
                sampleDestinations[0]
            ),

            UserActivity(
                TransactionType.DISTRIBUTION,
                LocalDateTime.parse("2021-03-25 16:15:19", formatter),
                sampleDestinations[1]
            ),

            UserActivity(
                TransactionType.DISCARD,
                LocalDateTime.parse("2021-12-28 08:01:56", formatter)
            ),

            UserActivity(
                TransactionType.DISTRIBUTION,
                LocalDateTime.parse("2021-05-28 18:00:20", formatter),
                sampleDestinations[2]
            ),

            UserActivity(
                TransactionType.DISTRIBUTION,
                LocalDateTime.parse("2021-10-10 12:11:56", formatter),
                sampleDestinations[3]
            ),

            UserActivity(
                TransactionType.DISTRIBUTION,
                LocalDateTime.parse("2021-02-02 08:20:11", formatter),
                sampleDestinations[4]
            ),

            UserActivity(
                TransactionType.CORRECTION,
                LocalDateTime.parse("2021-07-20 03:45:22", formatter)
            ),

            UserActivity(
                TransactionType.DISCARD,
                LocalDateTime.parse("2021-07-21 11:23:29", formatter)
            ),

            UserActivity(
                TransactionType.DISTRIBUTION,
                LocalDateTime.parse("2021-01-27 06:05:19", formatter),
                sampleDestinations[2]
            ),

            UserActivity(
                TransactionType.DISTRIBUTION,
                LocalDateTime.parse("2021-05-09 11:19:23", formatter),
                sampleDestinations[0]
            ),

            UserActivity(
                TransactionType.CORRECTION,
                LocalDateTime.parse("2021-02-28 05:00:10", formatter)
            ),

            UserActivity(
                TransactionType.DISTRIBUTION,
                LocalDateTime.parse("2021-04-21 14:47:09", formatter),
                sampleDestinations[4]
            ),

            UserActivity(
                TransactionType.CORRECTION,
                LocalDateTime.parse("2021-09-27 19:05:04", formatter)
            ),

            UserActivity(
                TransactionType.DISCARD,
                LocalDateTime.parse("2021-03-12 23:56:17", formatter)
            ),

            UserActivity(
                TransactionType.DISTRIBUTION,
                LocalDateTime.parse("2021-02-17 18:29:04", formatter),
                sampleDestinations[2]
            ),

            UserActivity(
                TransactionType.DISCARD,
                LocalDateTime.parse("2021-11-11 11:10:09", formatter)
            ),

            UserActivity(
                TransactionType.DISCARD,
                LocalDateTime.parse("2021-03-04 05:23:19", formatter)
            ),

            UserActivity(
                TransactionType.DISTRIBUTION,
                LocalDateTime.parse("2020-01-01 04:03:01", formatter),
                sampleDestinations[3]
            ),

            UserActivity(
                TransactionType.CORRECTION,
                LocalDateTime.parse("2021-01-12 08:31:11", formatter)
            ),

            UserActivity(
                TransactionType.DISTRIBUTION,
                LocalDateTime.parse("2021-06-09 08:02:05", formatter),
                sampleDestinations[4]
            ),
        )
    }

//    fun getAll(): LiveData<List<UserActivity>>
    fun add(activity: UserActivity) = activities.add(activity)
}