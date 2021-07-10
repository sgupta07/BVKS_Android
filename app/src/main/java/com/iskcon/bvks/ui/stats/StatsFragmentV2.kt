package com.iskcon.bvks.ui.stats

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.iskcon.bvks.R
import com.iskcon.bvks.listeners.StatsListener
import com.iskcon.bvks.manager.LectureManager
import com.iskcon.bvks.manager.StatsManager
import com.iskcon.bvks.model.ListenRecord
import com.iskcon.bvks.util.Constants
import com.iskcon.bvks.util.Utils
import com.iskcon.bvks.util.Utils.splitToComponentTimes
import kotlinx.android.synthetic.main.fragment_stats.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class StatsFragmentV2 : Fragment(), StatsListener, EasyPermissions.PermissionCallbacks {

    private lateinit var viewModel: StatsViewModel
    private val TAG = StatsFragmentV2::class.java.simpleName
    private var lastMonthStats: List<ListenRecord> = ArrayList()
    private var lastWeekStats: List<ListenRecord> = ArrayList()
    private var thisWeekStats: List<ListenRecord> = ArrayList()
    private var customStats: List<ListenRecord> = ArrayList()
    private var allStatsList = mutableListOf<ListenRecord>()
    private var startDateMillis = -1L
    private var endDateMillis = -1L
    private var bitmap: Bitmap? = null
    private var bitmapWithBorder: Bitmap? = null
    private var isMinutesMode: Boolean = true


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val viewModelFactory = StatsViewModelFactory()
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(StatsViewModel::class.java)
        return inflater.inflate(R.layout.fragment_stats, container, false)
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        tvTotalLecture.text = LectureManager.getInstance().totalLectureCount.toString()
        tvHeardLecture.text = LectureManager.getInstance().totalHeardLectures.toString()

        //get last month data
        StatsManager.getInstance().getUserListenDetails(StatsManager.StatsType.all, null, null, this);

        tvAllTime.setOnClickListener { //get  all data
            processAllStats(allStatsList)
            resetBothDates()
        }
        tvStartDate.setOnClickListener {
            Utils.showDatePicker(requireContext(), startDateMillis) {
                startDateMillis = it
                setDates(startDateMillis, endDateMillis)
                if (endDateMillis > 0) {
                    if (endDateMillis < startDateMillis) {
                        Utils.getInstance().showToast(context, "Selected date must be less than or same as Start Date.")
                        resetBothDates()
                    } else {
                        //get custom week data
                        setDates(startDateMillis, endDateMillis)
                        processCustomStats(allStatsList, startDateMillis, endDateMillis)

                    }
                }
            }
        }

        tvEndDate.setOnClickListener {
            Utils.showDatePicker(requireContext(), endDateMillis) {
                endDateMillis = it
                setDates(startDateMillis, endDateMillis)
                if (startDateMillis > 0) {
                    if (endDateMillis < startDateMillis) {
                        resetBothDates()
                        Utils.getInstance().showToast(context, "Selected date must be later than or same as Start Date.")
                    } else {
                        //get custom week data
                        setDates(startDateMillis, endDateMillis)
                        processCustomStats(allStatsList, startDateMillis, endDateMillis)

                    }
                }
            }
        }
        showMinutesMode()
    }

    private fun setDates(startDateMillis: Long, endDateMillis: Long) {
        if (startDateMillis > 0) {
            tvStartDate.text = Utils.getDate(startDateMillis, "dd-MM-yyyy")
        }
        if (endDateMillis > 0) {
            tvEndDate.text = Utils.getDate(endDateMillis, "dd-MM-yyyy")
        }

    }

    private fun resetBothDates() {
        tvEndDate.text = "End Date"
        tvStartDate.text = "Start Date"
        startDateMillis = -1L
        endDateMillis = -1L
    }

    private fun showMinutesMode() {
        llHoursLabel.visibility = View.GONE
        llMinutesLabel.visibility = View.VISIBLE
    }

    private fun showHoursMode() {
        llHoursLabel.visibility = View.VISIBLE
        llMinutesLabel.visibility = View.GONE
    }

    override fun statsUpdated(statsList: MutableList<ListenRecord>, statsType: StatsManager.StatsType?) {
        allStatsList = statsList
        //get last month data from stats list
        val (startDateMonth, endDateMonth) = getStartAndEndDateOfLastMonth()
        lastMonthStats = allStatsList.filter { it.date >= startDateMonth && it.date < endDateMonth }
        processLastMonthStats()
        //get last week data from stats list
        val (startDateWeek, endDateWeek) = getStartAndEndDateOfLastWeek()
        lastWeekStats = allStatsList.filter { it.date >= startDateWeek && it.date < endDateWeek }
        processLasWeekStats()
        //get this week data from stats list
        val (startDateCurrentWeek, endDateCurrentWeek) = getStartAndEndDateOfCurrentWeek()
        thisWeekStats = allStatsList.filter { it.date >= startDateCurrentWeek && it.date < endDateCurrentWeek }
        processThisWeekChart()
        //get all times records
        processAllStats(allStatsList)


    }

    override fun onError(msg: String?) {

    }

    //region Process this Week Chart
    private fun processThisWeekChart() {
        var audioValue = mutableListOf<Double>()
        var videoValue = mutableListOf<Double>()
        var dates = mutableListOf<String>()
        thisWeekStats.forEach {
            //Set Day name
            dates.add("${it.dateOfRecord.day} ${Utils.monthName(it.dateOfRecord.month)}")
            //Set Audio Details
            val audioArray = it.audioListen
            print(audioArray)
            val watch = Utils.graphTimeManage(audioArray)
            print("H" + watch[0])
            print("M" + watch[1])
            print("S" + watch[2])
            var aValue = 0.0
            aValue = if (watch[1] >= 10) {
                "${watch[0]}.${watch[1]}".toDouble()
            } else {
                "${watch[0]}.0${watch[1]}".toDouble()
            }
            audioValue.add(aValue)
            //Set Video Details
            val videoArray = it.videoListen
            print(videoArray)
            val vwatch = Utils.graphTimeManage(videoArray)
            print("H" + vwatch[0])
            print("M" + vwatch[1])
            print("S" + vwatch[2])

            var vValue = 0.0
            vValue = if (watch[1] >= 10) {
                "${vwatch[0]}.${vwatch[1]}".toDouble()
            } else {
                "${vwatch[0]}.0${vwatch[1]}".toDouble()
            }
            videoValue.add(vValue)
            if (aValue >= 1.0 || vValue >= 1.0) {
                isMinutesMode = false
                showHoursMode()
            }

        }
        print(dates)
        print(audioValue)
        print(videoValue)
        //revers list
        dates = dates.reversed().toMutableList()
        audioValue = audioValue.reversed().toMutableList()
        videoValue = videoValue.reversed().toMutableList()
        if (isMinutesMode) {
            audioValue = audioValue.map { it * 100 }.toMutableList()
            videoValue = videoValue.map { it * 100 }.toMutableList()
            print(dates)
            print(audioValue)
            print(videoValue)
        }
        setGroupBarChart(audioValue, videoValue, dates)
    }
    //endregion

    //region Set Group Bar Chart
    private fun setGroupBarChart(audioVal: MutableList<Double>, videoVal: MutableList<Double>, unitName: MutableList<String>) {
        thisWeekChart.setNoDataText("You need to provide data for the chart.")
        //legend
        /*thisWeekChart.legend.isEnabled = true
        thisWeekChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        thisWeekChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        thisWeekChart.legend.orientation = Legend.LegendOrientation.VERTICAL
        thisWeekChart.legend.setDrawInside(true)
        thisWeekChart.legend.yOffset = 10.0f;
        thisWeekChart.legend.xOffset = 10.0f;
        thisWeekChart.legend.yEntrySpace = 0.0f;*/

        thisWeekChart.legend.isEnabled = true
        thisWeekChart.legend.direction = Legend.LegendDirection.RIGHT_TO_LEFT
        thisWeekChart.description.isEnabled = false
        //X axis
        var xaxis = thisWeekChart.xAxis
        xaxis.setDrawGridLines(true)
        xaxis.position = XAxis.XAxisPosition.BOTTOM
        xaxis.setCenterAxisLabels(true)
        xaxis.valueFormatter = IndexAxisValueFormatter(unitName)
        xaxis.granularity = 1.0f
        //Y axis
        val leftAxisFormatter = /*NumberFormatter()*/12
        /*leftAxisFormatter.minimumFractionDigits = 0
        leftAxisFormatter.maximumFractionDigits = 24
        leftAxisFormatter.alwaysShowsDecimalSeparator = false*/

        val yaxis = thisWeekChart.axisLeft
        yaxis.valueFormatter = DefaultAxisValueFormatter(leftAxisFormatter)
        yaxis.axisMinimum = 0.0f
        yaxis.granularity = 1.0f
        yaxis.setDrawGridLines(false)
        thisWeekChart.axisRight.isEnabled = false
        setChart(audioVal, videoVal, unitName)
    }
    //endregion

    //region Set Chart
    private fun setChart(audioVal: MutableList<Double>, videoVal: MutableList<Double>, unitName: MutableList<String>) {
        val dataEntries = ArrayList<BarEntry>()
        val dataEntries1 = ArrayList<BarEntry>()
        unitName.forEachIndexed { i, s ->
            val dataEntry = BarEntry(i.toFloat(), audioVal[i].toFloat())
            dataEntries.add(dataEntry)
            val dataEntry1 = BarEntry(i.toFloat(), videoVal[i].toFloat())
            dataEntries1.add(dataEntry1)
        }

        val audioDataSet = BarDataSet(dataEntries, "Audio")
        val videoDataSet = BarDataSet(dataEntries1, "Video")
        audioDataSet.color = Color.parseColor("#f96d00")
        videoDataSet.color = Color.rgb(164, 228, 251)
        audioDataSet.setDrawValues(false)
        videoDataSet.setDrawValues(false)

        val chartData = BarData(audioDataSet, videoDataSet)
        val groupSpace = 0.3f
        val barSpace = 0.05f
        val barWidth = 0.3f
        val groupCount = unitName.size
        val startYear = 0.0f
        chartData.barWidth = barWidth
        thisWeekChart.xAxis.axisMinimum = startYear
        val gg = chartData.getGroupWidth(groupSpace, barSpace)
        thisWeekChart.xAxis.axisMaximum = startYear + gg * groupCount
        chartData.groupBars(startYear, groupSpace, barSpace)
        thisWeekChart.notifyDataSetChanged()
        thisWeekChart.data = chartData
        thisWeekChart.invalidate()
    }
    //endregion

    //region Process All Stats
    @SuppressLint("SetTextI18n")
    fun processAllStats(allStatsList: MutableList<ListenRecord>) {

        var videoHr = 0
        var audioHr = 0;
        allStatsList.forEach {
            videoHr += it.videoListen
            audioHr += it.audioListen
        }

        val timeVideo = splitToComponentTimes(videoHr.toLong());
        val timeAudio = splitToComponentTimes(audioHr.toLong());

        tvVideoHr.text = "${timeVideo[0]}h ${timeVideo[1]}m"
        tvAudioHr.text = "${timeAudio[0]}h ${timeAudio[1]}m"
    }
    //endregion

    //region Custom Stats
    private fun processCustomStats(allStatsList: MutableList<ListenRecord>, startDateMillis: Long, endDateMillis: Long) {
        val (startDateCustom, endDateCustom) = getStartAndEndDateOfMillis(startDateMillis, endDateMillis)
        customStats = allStatsList.filter { it!!.date >= startDateCustom && it.date < endDateCustom }
        var videoHr = 0
        var audioHr = 0
        customStats.forEach {
            videoHr += it.videoListen
            audioHr += it.audioListen
        }

        val timeVideo = splitToComponentTimes(videoHr.toLong());
        val timeAudio = splitToComponentTimes(audioHr.toLong());

        tvVideoHr.text = "${timeVideo[0]}h ${timeVideo[1]}m"
        tvAudioHr.text = "${timeAudio[0]}h ${timeAudio[1]}m"
    }
    //endregion

    //region Process Last Month Stats
    private fun processLastMonthStats() {
        val finalPoint = 24 * lastMonthStats.size * 3600
        var sbCount = 0;
        var bgCount = 0;
        var ccCount = 0;
        var vnsCount = 0;
        var bhajanCount = 0;
        var audioListenCount = 0;
        var videoListenCount = 0;

        lastMonthStats.forEach {
            sbCount += it.listenDetails.SB
            bgCount += it.listenDetails.BG
            ccCount += it.listenDetails.CC
            vnsCount += it.listenDetails.VSN
            bhajanCount += it.listenDetails.others
            audioListenCount += it.audioListen
            videoListenCount += it.videoListen
        }

        val sbTime = splitToComponentTimes(sbCount.toLong())
        val bgTime = splitToComponentTimes(bgCount.toLong())
        val ccTime = splitToComponentTimes(ccCount.toLong())
        val vsnTime = splitToComponentTimes(vnsCount.toLong())
        val bhajanTime = splitToComponentTimes(bhajanCount.toLong())
        val audioTime = splitToComponentTimes(audioListenCount.toLong())
        val videoTime = splitToComponentTimes(videoListenCount.toLong())

        tvLmSb.text = "SB ${sbTime[0]}h"
        tvLmBg.text = "BG ${bgTime[0]}h"
        tvLmCC.text = "CC ${ccTime[0]}h"
        tvLmVns.text = "VSN ${vsnTime[0]}h"
        tvLmBhajan.text = "Bhajans ${bhajanTime[0]}h"


        pbLastMonthAudio.max = 100
        if (finalPoint > 0) {
            pbLastMonthAudio.progress = (audioListenCount * 100) / finalPoint
        } else {
            pbLastMonthAudio.progress = 0

        }
        pbLastMonthVideo.max = 100
        if (finalPoint > 0) {
            pbLastMonthVideo.progress = (videoListenCount * 100) / finalPoint

        } else {
            pbLastMonthVideo.progress = 0

        }

        tvLmAudioTime.text = "${audioTime[0]}h ${audioTime[1]}m ${audioTime[2]}s"
        tvLmVideoTime.text = "${videoTime[0]}h ${videoTime[1]}m ${videoTime[2]}s"
    }
    //endregion

    //region Process Last week Stats
    private fun processLasWeekStats() {
        val finalPoint = 24 * lastWeekStats.size * 3600
        var sbCount = 0;
        var bgCount = 0;
        var ccCount = 0;
        var vnsCount = 0;
        var bhajanCount = 0;
        var audioListenCount = 0;
        var videoListenCount = 0;

        lastWeekStats.forEach {
            sbCount += it.listenDetails.SB
            bgCount += it.listenDetails.BG
            ccCount += it.listenDetails.CC
            vnsCount += it.listenDetails.VSN
            bhajanCount += it.listenDetails.others

            audioListenCount += it.audioListen
            videoListenCount += it.videoListen
        }

        val sbTime = splitToComponentTimes(sbCount.toLong())
        val bgTime = splitToComponentTimes(bgCount.toLong())
        val ccTime = splitToComponentTimes(ccCount.toLong())
        val vsnTime = splitToComponentTimes(vnsCount.toLong())
        val bhajanTime = splitToComponentTimes(bhajanCount.toLong())
        val audioTime = splitToComponentTimes(audioListenCount.toLong())
        val videoTime = splitToComponentTimes(videoListenCount.toLong())

        tvLwSb.text = "SB ${sbTime[0]}h"
        tvLwBg.text = "BG ${bgTime[0]}h"
        tvLwCC.text = "CC ${ccTime[0]}h"
        tvLwVns.text = "VSN ${vsnTime[0]}h"
        tvLwBhajan.text = "Bhajans ${bhajanTime[0]}h"


        pbLastWeekVideo.progress = videoListenCount

        pbLastWeekAudio.max = 100
        if (finalPoint > 0) {
            pbLastWeekAudio.progress = (audioListenCount * 100) / finalPoint
        } else {
            pbLastWeekAudio.progress = 0
        }
        pbLastWeekVideo.max = 100
        if (finalPoint > 0) {
            pbLastWeekVideo.progress = (videoListenCount * 100) / finalPoint
        } else {
            pbLastWeekVideo.progress = 0
        }


        tvLwAudioTime.text = "${audioTime[0]}h ${audioTime[1]}m ${audioTime[2]}s"
        tvLwVideoTime.text = "${videoTime[0]}h ${videoTime[1]}m ${videoTime[2]}s"
    }
    //endregion

    private fun getStartAndEndDateOfLastMonth(): FirstAndLast {
        val aCalendar = Calendar.getInstance()
        // add -1 month to current month
        aCalendar.add(Calendar.MONTH, -1)
        // set DATE to 1, so first date of previous month
        aCalendar[Calendar.DATE] = 1
        setTimeToBeginningOfDay(aCalendar)
        val firstDateOfPreviousMonth = aCalendar.time
        // set actual maximum date of previous month
        aCalendar[Calendar.DATE] = aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        //read it
        setTimeToEndofDay(aCalendar)
        val lastDateOfPreviousMonth = aCalendar.time
        return FirstAndLast(firstDateOfPreviousMonth, lastDateOfPreviousMonth)
    }

    private fun getStartAndEndDateOfLastWeek(): FirstAndLast {
        // Calendar object
        val cal = Calendar.getInstance()
        // "move" cal to sunday this week (i understand it this way)
        cal[Calendar.DAY_OF_WEEK] = Calendar.SUNDAY
        // calculate monday week ago (moves cal 7 days back)
        cal.add(Calendar.DATE, -7)
        setTimeToBeginningOfDay(cal)
        val firstDateOfPreviousWeek = cal.time
        // calculate sunday last week (moves cal 6 days fwd)
        cal.add(Calendar.DATE, 6)
        setTimeToEndofDay(cal)
        val lastDateOfPreviousWeek = cal.time
        return FirstAndLast(firstDateOfPreviousWeek, lastDateOfPreviousWeek)
    }

    private fun getStartAndEndDateOfCurrentWeek(): FirstAndLast {
        val currentCalendar = Calendar.getInstance()
        currentCalendar[Calendar.DAY_OF_WEEK] = Calendar.SUNDAY
        setTimeToBeginningOfDay(currentCalendar)
        val currentWeekStart = currentCalendar.time

        currentCalendar.add(Calendar.DATE, 6) //add 6 days after Sunday
        setTimeToEndofDay(currentCalendar)
        val currentWeekEnd = currentCalendar.time
        return FirstAndLast(currentWeekStart, currentWeekEnd)
    }

    private fun getStartAndEndDateOfMillis(startDateMillis: Long, endDateMillis: Long): FirstAndLast {
        val currentCalendar = Calendar.getInstance()
        currentCalendar.timeInMillis = startDateMillis
        setTimeToBeginningOfDay(currentCalendar)
        val currentWeekStart = currentCalendar.time

        currentCalendar.timeInMillis = endDateMillis
        setTimeToEndofDay(currentCalendar)
        val currentWeekEnd = currentCalendar.time
        return FirstAndLast(currentWeekStart, currentWeekEnd)
    }

    private fun setTimeToBeginningOfDay(calendar: Calendar) {
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
    }

    private fun setTimeToEndofDay(calendar: Calendar) {
        calendar[Calendar.HOUR_OF_DAY] = 23
        calendar[Calendar.MINUTE] = 59
        calendar[Calendar.SECOND] = 59
        calendar[Calendar.MILLISECOND] = 999
    }

    data class FirstAndLast(var startDate: Date, var endDate: Date)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.stats_menu, menu)
        val item = menu.findItem(R.id.action_share_stats)
        item.actionView.setOnClickListener {
            if (EasyPermissions.hasPermissions(
                            requireContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    )
            ) {
                createBitmapFromLayout()
            } else {
                val perRequest = PermissionRequest.Builder(this, Constants.RC_WRITE_STORAGE_PERMISSION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        .setRationale(getString(R.string.permission_message_storage))
                        .setTheme(R.style.MyDatePickerStyle)
                        .build()
                EasyPermissions.requestPermissions(perRequest)
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share_stats -> {

                true
            }

            else -> false
        }
    }

    private fun createBitmapFromLayout() {
        bitmap = loadBitmapFromView(cl_main, cl_main.width, cl_main.height)
        //bitmapWithBorder = addBorderToBitmap(bitmap!!, 50, Color.WHITE)
        bitmapWithBorder = bitmap

        createPdf()
    }

    private fun addBorderToBitmap(srcBitmap: Bitmap, borderWidth: Int, borderColor: Int): Bitmap? {
        val dstBitmap = Bitmap.createBitmap(
                srcBitmap.width + borderWidth * 2,  // Width
                srcBitmap.height + borderWidth * 2,  // Height
                Bitmap.Config.ARGB_8888 // Config
        )
        val canvas = Canvas(dstBitmap)
        val paint = Paint()
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth.toFloat()
        paint.isAntiAlias = true
        val rect = Rect(
                borderWidth / 2,
                borderWidth / 2,
                canvas.width - borderWidth / 2,
                canvas.height - borderWidth / 2
        )
        canvas.drawRect(rect, paint)
        canvas.drawBitmap(srcBitmap, borderWidth.toFloat(), borderWidth.toFloat(), null)
        srcBitmap.recycle()
        return dstBitmap
    }

    private fun loadBitmapFromView(v: View, width: Int, height: Int): Bitmap? {
        val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        v.draw(c)
        return b
    }

    private fun createPdf() {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels.toFloat()
        val width = displayMetrics.widthPixels.toFloat()
        val convertHeight = height.toInt()
        val convertWidth = width.toInt()

        val document = PdfDocument()
        val pageInfo = PageInfo.Builder(convertWidth, convertHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        canvas.drawPaint(paint)
        bitmapWithBorder = Bitmap.createScaledBitmap(bitmapWithBorder!!, convertWidth, convertHeight, true)
        paint.setColor(Color.BLUE)
        canvas.drawBitmap(bitmapWithBorder!!, 0f, 0f, null)
        document.finishPage(page)

        // write the document content
        val targetPdf = "/sdcard/statsResult.pdf"
        val filePath: File
        filePath = File(targetPdf)
        try {
            document.writeTo(FileOutputStream(filePath))
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show()
        }

        // close the document
        document.close()
        Toast.makeText(requireContext(), "PDF of result is created!!!", Toast.LENGTH_SHORT).show()
//        openGeneratedPDF()
        shareGeneratedPDF()
    }

    private fun shareGeneratedPDF() {
        val file = File("/sdcard/statsResult.pdf")
        val intentShareFile = Intent(Intent.ACTION_SEND)
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(context!!, context!!.applicationContext.packageName.toString() + ".provider", file)
            intentShareFile.type = "application/pdf"
            intentShareFile.putExtra(Intent.EXTRA_STREAM, uri)
            intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                    "Sharing Result ")
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing Result")
            context!!.startActivity(Intent.createChooser(intentShareFile, "Share Result"))
        }
    }

    private fun openGeneratedPDF() {
        val file = File("/sdcard/statsResult.pdf")
        if (file.exists()) {
            val intent = Intent(Intent.ACTION_VIEW)
//            val uri: Uri = Uri.fromFile(file)
            val uri = FileProvider.getUriForFile(context!!, context!!.applicationContext.packageName.toString() + ".provider", file)
            intent.setDataAndType(uri, "application/pdf")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), "No Application available to view pdf", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        when (requestCode) {
            Constants.RC_WRITE_STORAGE_PERMISSION -> if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                AppSettingsDialog.Builder(this).build().show()
            } else {
                Utils.getInstance().showToast(requireContext(), "Permission Denied, We can not share your result")
            }
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == Constants.RC_WRITE_STORAGE_PERMISSION) {
            createBitmapFromLayout()
        }
    }
}