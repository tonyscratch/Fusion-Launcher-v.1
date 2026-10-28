package com.fusion.launcher

import android.app.Activity
import android.app.AlertDialog
import android.app.WallpaperManager
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Fusion Launcher: native Android HOME replacement, six independently styled modes. */
class MainActivity : Activity() {
    private val modes = listOf("Samsung One UI", "Windows Desktop", "macOS", "iOS", "Fusion Hybrid", "Nokia Lumia")
    private val prefs by lazy { getSharedPreferences("fusion_prefs", Context.MODE_PRIVATE) }
    private lateinit var root: LinearLayout
    private var page = "home"
    private var mode: Int get() = prefs.getInt("mode", 4).coerceIn(0,5); set(v) = save("mode",v)
    private var accent: Int get() = prefs.getInt("accent",0xFF00A4EF.toInt()); set(v) = save("accent",v)
    private var columns: Int get() = prefs.getInt("columns",4).coerceIn(3,7); set(v) = save("columns",v)
    private var dark: Boolean get() = prefs.getBoolean("dark",true); set(v) = save("dark",v)
    private var tiles: Boolean get() = prefs.getBoolean("tiles",false); set(v) = save("tiles",v)
    private var lumia10: Boolean get() = prefs.getBoolean("lumia10",false); set(v) = save("lumia10",v)
    private var labels: Boolean get() = prefs.getBoolean("labels",true); set(v) = save("labels",v)
    private var clock: Boolean get() = prefs.getBoolean("clock",true); set(v) = save("clock",v)
    private var rounded: Boolean get() = prefs.getBoolean("rounded",true); set(v) = save("rounded",v)
    private var showDock: Boolean get() = prefs.getBoolean("dock",true); set(v) = save("dock",v)
    private var compact: Boolean get() = prefs.getBoolean("compact",false); set(v) = save("compact",v)
    private var wallpaper: Boolean get() = prefs.getBoolean("wallpaper",false); set(v) = save("wallpaper",v)
    private val fg get() = if(dark) Color.WHITE else 0xFF172033.toInt()
    private val bg get() = if(dark) 0xFF101522.toInt() else 0xFFF3F5F9.toInt()
    private val panel get() = if(dark) 0xFF263047.toInt() else Color.WHITE
    private fun save(k:String,v:Int){prefs.edit().putInt(k,v).apply()}
    private fun save(k:String,v:Boolean){prefs.edit().putBoolean(k,v).apply()}
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun shape(color:Int,r:Int=16)=GradientDrawable().apply { setColor(color); cornerRadius=dp(r).toFloat() }
    private fun txt(s:String,size:Float=15f,bold:Boolean=false)=TextView(this).apply {
        text=s; textSize=size; setTextColor(fg); gravity=Gravity.CENTER_VERTICAL
        if(bold) setTypeface(null,Typeface.BOLD)
    }
    private fun col()=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
    private fun row()=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
    private fun gap(h:Int)=Space(this).apply { layoutParams=LinearLayout.LayoutParams(1,dp(h)) }
    private fun button(s:String, action:()->Unit)=TextView(this).apply {
        text=s; textSize=14f; setTextColor(Color.WHITE); gravity=Gravity.CENTER
        setPadding(dp(12),dp(12),dp(12),dp(12)); background=shape(accent,12)
        setOnClickListener { action() }
    }
    data class App(val name:String,val component:ComponentName)
    private fun allApps():List<App> {
        val q=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION") val list=packageManager.queryIntentActivities(q,0)
        return list.mapNotNull { r ->
            val a=r.activityInfo ?: return@mapNotNull null
            if(a.packageName==packageName) return@mapNotNull null
            App(r.loadLabel(packageManager).toString(),ComponentName(a.packageName,a.name))
        }.distinctBy { it.component.flattenToString() }.sortedBy { it.name.lowercase(Locale.getDefault()) }
    }
    private fun hidden()=prefs.getStringSet("hidden",emptySet()) ?: emptySet()
    private fun pinned()=prefs.getString("pinned","")!!.split('|').filter { it.isNotBlank() }
    private fun togglePinned(c:ComponentName){
        val k=c.flattenToString(); val a=pinned().toMutableList()
        if(k in a) a.remove(k) else a.add(k)
        prefs.edit().putString("pinned",a.joinToString("|")).apply(); home()
    }
    private fun toggleHidden(c:ComponentName){
        val k=c.flattenToString(); val a=hidden().toMutableSet()
        if(k in a) a.remove(k) else a.add(k)
        prefs.edit().putStringSet("hidden",a).apply(); home()
    }
    private fun launch(a:App){
        try {
            val i=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(a.component).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            startActivity(i)
        } catch(e:Exception) { Toast.makeText(this,"Unable to open ${a.name}",Toast.LENGTH_SHORT).show() }
    }
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);home()}
    override fun onNewIntent(intent:Intent){super.onNewIntent(intent);home()}
    private fun requestDefault(){
        if(Build.VERSION.SDK_INT>=29){
            val rm=getSystemService(RoleManager::class.java)
            if(rm.isRoleAvailable(RoleManager.ROLE_HOME) && !rm.isRoleHeld(RoleManager.ROLE_HOME))
                startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_HOME),91)
        } else startActivity(Intent("android.settings.HOME_SETTINGS"))
    }
    private fun layout(title:String){
        root=col().apply { setPadding(dp(16),dp(18),dp(16),dp(20)); setBackgroundColor(bg) }
        val sc=ScrollView(this).apply { fillViewport=true; addView(root) }
        setContentView(sc)
        val top=row(); val t=txt(title,if(mode==5)30f else 23f,true)
        top.addView(t,LinearLayout.LayoutParams(0,dp(52),1f))
        top.addView(button("◈") { modesPage() }); top.addView(gapHorizontal(6)); top.addView(button("⚙") { settings() })
        root.addView(top);root.addView(gap(10))
    }
    private fun gapHorizontal(w:Int)=Space(this).apply { layoutParams=LinearLayout.LayoutParams(dp(w),1) }
    private fun section(s:String){root.addView(gap(12));root.addView(txt(s,18f,true));root.addView(gap(10))}
    private fun home(){
        page="home"; layout(if(mode==5) "Start" else modes[mode])
        when(mode){
            0 -> root.addView(txt("One UI  •  Your Galaxy-inspired space",13f))
            1 -> root.addView(txt("⊞  Start  •  Desktop workspace",14f))
            2 -> root.addView(txt("⌕  Spotlight  •  Applications",14f))
            3 -> root.addView(txt("App Library  •  Home",14f))
            4 -> root.addView(txt("Fusion  •  Make it yours",14f))
            5 -> root.addView(txt(if(lumia10) "Windows 10 Mobile" else "Windows Phone 8.1",13f))
        }
        if(clock){
            root.addView(gap(12))
            val now=SimpleDateFormat("HH:mm",Locale.getDefault()).format(Date())
            val day=SimpleDateFormat("EEEE, d MMMM",Locale.getDefault()).format(Date())
            root.addView(txt(now,if(mode==5)46f else 34f,true));root.addView(txt(day,13f))
        }
        root.addView(gap(16))
        val search=EditText(this).apply {
            hint=if(mode==2) "Spotlight search" else "Search apps"
            setSingleLine(true);textSize=15f;setTextColor(fg);setHintTextColor(Color.GRAY)
            setPadding(dp(14),dp(10),dp(14),dp(10));background=shape(panel,if(mode==5)0 else 16)
        }
        root.addView(search);root.addView(gap(12))
        val all=allApps().filter { it.component.flattenToString() !in hidden() }
        val fav=pinned(); val ordered=all.sortedWith(compareBy<App> { if(it.component.flattenToString() in fav) 0 else 1 }.thenBy { it.name.lowercase() })
        val grid=GridLayout(this).apply { columnCount=if(mode==5) 4 else columns }
        root.addView(grid)
        fun fill(query:String){
            grid.removeAllViews()
            val filtered=ordered.filter { it.name.contains(query,true) }
            if(filtered.isEmpty()) { grid.addView(txt("No matching apps",14f));return }
            val n=if(mode==5)4 else columns
            val screen=resources.displayMetrics.widthPixels
            val width=((screen-dp(32))/n)-dp(8)
            filtered.forEach { a ->
                val tile=col().apply {
                    gravity=Gravity.CENTER;setPadding(dp(3),dp(7),dp(3),dp(6))
                    background=shape(if(mode==5&&!tiles)accent else panel,if(mode==5)0 else if(rounded)18 else 4)
                }
                val iv=ImageView(this).apply {
                    try { setImageDrawable(packageManager.getActivityIcon(a.component)) } catch(_:Exception){}
                    scaleType=ImageView.ScaleType.FIT_CENTER
                }
                tile.addView(iv,LinearLayout.LayoutParams(dp(if(compact)32 else 42),dp(if(compact)32 else 42)))
                if(labels){
                    val label=txt(a.name,if(compact)10f else 11f).apply {
                        gravity=Gravity.CENTER;maxLines=2;ellipsize=android.text.TextUtils.TruncateAt.END
                    }
                    tile.addView(label,LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(30)))
                }
                tile.setOnClickListener { launch(a) }
                tile.setOnLongClickListener {
                    val isPin=a.component.flattenToString() in pinned()
                    AlertDialog.Builder(this).setTitle(a.name)
                        .setItems(arrayOf(if(isPin) "Unpin" else "Pin to top","Hide from launcher","App info")) { _,which ->
                            when(which){
                                0->togglePinned(a.component)
                                1->toggleHidden(a.component)
                                2->{val i=Intent("android.settings.APPLICATION_DETAILS_SETTINGS",android.net.Uri.parse("package:${a.component.packageName}"));startActivity(i)}
                            }
                        }.show();true
                }
                grid.addView(tile,GridLayout.LayoutParams().apply {
                    this.width=width;height=dp(if(compact)75 else if(mode==5)110 else 96)
                    setMargins(dp(4),dp(4),dp(4),dp(4))
                })
            }
        }
        fill("")
        search.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s:CharSequence?,start:Int,count:Int,after:Int){}
            override fun onTextChanged(s:CharSequence?,start:Int,before:Int,count:Int)=fill(s?.toString() ?: "")
            override fun afterTextChanged(s:Editable?){}
        })
        if(showDock){
            section(if(mode==1) "Taskbar" else if(mode==2) "Dock" else "Quick dock")
            val bar=row().apply { background=shape(panel,if(rounded)22 else 4);setPadding(dp(8),dp(8),dp(8),dp(8)) }
            bar.addView(button("⌂ Home") { home() },LinearLayout.LayoutParams(0,dp(44),1f))
            bar.addView(gapHorizontal(8))
            bar.addView(button("◈ Modes") { modesPage() },LinearLayout.LayoutParams(0,dp(44),1f))
            bar.addView(gapHorizontal(8))
            bar.addView(button("⚙ Studio") { settings() },LinearLayout.LayoutParams(0,dp(44),1f))
            root.addView(bar)
        }
    }
    private fun modesPage(){
        page="modes";layout("Interface modes")
        modes.forEachIndexed { i,name ->
            root.addView(button((if(mode==i) "✓  " else "")+name) { mode=i;home() });root.addView(gap(9))
        }
        root.addView(txt("Switch modes without changing your installed Android operating system.",13f))
    }
    private fun toggle(title:String,value:Boolean,change:()->Unit){
        root.addView(button("${if(value) "✓" else "○"}  $title") { change();settings() });root.addView(gap(7))
    }
    private fun settings(){
        page="settings";layout("Fusion Studio")
        section("Appearance")
        toggle("Dark theme",dark){dark=!dark}
        toggle("Rounded app cards",rounded){rounded=!rounded}
        toggle("Show app labels",labels){labels=!labels}
        toggle("Show clock",clock){clock=!clock}
        toggle("Show dock",showDock){showDock=!showDock}
        toggle("Compact layout",compact){compact=!compact}
        section("Grid columns: $columns")
        val slider=SeekBar(this).apply { max=4;progress=columns-3 }
        slider.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(v:SeekBar?,p:Int,fromUser:Boolean){if(fromUser)columns=p+3}
            override fun onStartTrackingTouch(v:SeekBar?){}
            override fun onStopTrackingTouch(v:SeekBar?){settings()}
        });root.addView(slider)
        section("Accent colors")
        val palette=listOf(0xFF00A4EF.toInt(),0xFF00A300.toInt(),0xFFE51400.toInt(),0xFFAA00FF.toInt(),0xFFF0A30A.toInt(),0xFFE671B8.toInt(),0xFF008A8A.toInt(),0xFF76608A.toInt(),0xFF3F80FF.toInt())
        palette.forEach { c ->
            val hex=String.format("#%06X",c and 0xFFFFFF)
            root.addView(button("${if(accent==c) "✓  " else ""}$hex") { accent=c;settings() });root.addView(gap(5))
        }
        section("Nokia Lumia")
        toggle("Windows 10 Mobile style (off = WP 8.1)",lumia10){lumia10=!lumia10}
        toggle("Transparent tiles",tiles){tiles=!tiles}
        section("Apps & device")
        root.addView(button("Manage hidden apps") { manageHidden() });root.addView(gap(8))
        root.addView(button("Choose default home app") { requestDefault() });root.addView(gap(8))
        root.addView(button("Android wallpaper picker") {
            try {startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))}
            catch(_:Exception){try{startActivity(Intent(Intent.ACTION_SET_WALLPAPER))}catch(_:Exception){Toast.makeText(this,"Wallpaper picker unavailable",Toast.LENGTH_SHORT).show()}}
        });root.addView(gap(8))
        root.addView(button("Export settings (share JSON)") { exportSettings() });root.addView(gap(8))
        root.addView(button("Import settings (paste JSON)") { importSettings() });root.addView(gap(8))
        root.addView(button("Reset launcher settings") {
            AlertDialog.Builder(this).setTitle("Reset settings?").setMessage("This clears launcher preferences and pinned apps.")
                .setPositiveButton("Reset") { _,_->prefs.edit().clear().apply();home()}.setNegativeButton("Cancel",null).show()
        });root.addView(gap(12));root.addView(button("Return home") { home() })
    }
    private fun manageHidden(){
        page="hidden";layout("Hidden apps")
        val hidden=hidden()
        val matches=allApps().filter { it.component.flattenToString() in hidden }
        if(matches.isEmpty()) root.addView(txt("No hidden apps",15f))
        matches.forEach { a ->root.addView(button("Unhide ${a.name}") { toggleHidden(a.component);manageHidden() });root.addView(gap(8)) }
        root.addView(button("Back to studio") { settings() })
    }
    private fun exportSettings(){
        val o=JSONObject()
        listOf("mode","accent","columns").forEach { k ->o.put(k,prefs.getInt(k,if(k=="columns")4 else 0)) }
        listOf("dark","tiles","lumia10","labels","clock","rounded","dock","compact").forEach { k ->o.put(k,prefs.getBoolean(k,true)) }
        o.put("pinned",prefs.getString("pinned",""));o.put("hidden",JSONArray(hidden().toList()))
        val send=Intent(Intent.ACTION_SEND).apply {type="text/plain";putExtra(Intent.EXTRA_TEXT,o.toString(2))}
        startActivity(Intent.createChooser(send,"Export Fusion settings"))
    }
    private fun importSettings(){
        val input=EditText(this).apply {hint="Paste exported Fusion JSON";minLines=4}
        AlertDialog.Builder(this).setTitle("Import settings").setView(input)
            .setPositiveButton("Import") { _,_->
                try {
                    val o=JSONObject(input.text.toString());val e=prefs.edit()
                    listOf("mode","accent","columns").forEach { if(o.has(it)) e.putInt(it,o.getInt(it)) }
                    listOf("dark","tiles","lumia10","labels","clock","rounded","dock","compact").forEach { if(o.has(it)) e.putBoolean(it,o.getBoolean(it)) }
                    if(o.has("pinned"))e.putString("pinned",o.getString("pinned"))
                    if(o.has("hidden")){val a=o.getJSONArray("hidden");e.putStringSet("hidden",(0 until a.length()).map { a.getString(it) }.toSet())}
                    e.apply();home()
                } catch(_:Exception){Toast.makeText(this,"Invalid settings JSON",Toast.LENGTH_LONG).show()}
            }.setNegativeButton("Cancel",null).show()
    }
    @Deprecated("Legacy back handling") override fun onBackPressed(){home()}
}
