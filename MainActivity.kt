package com.focuslife.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject

private val Bg = Color(0xFF101522)
private val Panel = Color(0xFF1B2638)
private val Accent = Color(0xFF8DABFF)
private val Mint = Color(0xFF68D6B3)
private val Muted = Color(0xFFAAB7CE)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("focuslife", MODE_PRIVATE)
        setContent {
            var english by remember { mutableStateOf(prefs.getBoolean("english", true)) }
            var tab by remember { mutableStateOf(0) }
            var tasks by remember { mutableStateOf(loadList(prefs.getString("tasks", "[]")!!)) }
            var notes by remember { mutableStateOf(loadList(prefs.getString("notes", "[]")!!)) }
            var habits by remember { mutableStateOf(loadList(prefs.getString("habits", "[]")!!)) }
            MaterialTheme(colorScheme = darkColorScheme(
                background = Bg, surface = Panel, primary = Accent,
                secondary = Mint, onBackground = Color.White, onSurface = Color.White
            )) {
                Scaffold(containerColor = Bg, bottomBar = {
                    NavigationBar(containerColor = Panel) {
                        val labels = if (english) listOf("Home","Tasks","Notes","Habits","Focus") else listOf("হোম","কাজ","নোট","অভ্যাস","ফোকাস")
                        val icons = listOf(Icons.Default.Home, Icons.Default.CheckCircle, Icons.Default.EditNote, Icons.Default.LocalFireDepartment, Icons.Default.Timer)
                        labels.forEachIndexed { i, label ->
                            NavigationBarItem(selected = tab == i, onClick = { tab = i },
                                icon = { Icon(icons[i], null) }, label = { Text(label, fontSize = 10.sp) })
                        }
                    }
                }) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding).padding(18.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("FocusLife", fontSize = 27.sp, color = Color.White)
                                Text("PLAN • DO • GROW", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp)
                            }
                            TextButton(onClick = {
                                english = !english
                                prefs.edit().putBoolean("english", english).apply()
                            }) { Text(if (english) "বাংলা" else "English") }
                        }
                        Spacer(Modifier.height(20.dp))
                        when(tab) {
                            0 -> HomeScreen(english, tasks.size, tasks.count { it.startsWith("✓") }, habits.size) { tab = 1 }
                            1 -> ListScreen(english, if(english) "Daily Tasks" else "দৈনিক কাজ", tasks, "task", { tasks = it; prefs.edit().putString("tasks", saveList(it)).apply() })
                            2 -> ListScreen(english, if(english) "My Notes" else "আমার নোট", notes, "note", { notes = it; prefs.edit().putString("notes", saveList(it)).apply() })
                            3 -> ListScreen(english, if(english) "Habit Tracker" else "অভ্যাস ট্র্যাকার", habits, "habit", { habits = it; prefs.edit().putString("habits", saveList(it)).apply() })
                            else -> FocusScreen(english)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(en: Boolean, total: Int, done: Int, habits: Int, openTasks: () -> Unit) {
    Text(if(en) "Good day! 👋" else "শুভ দিন! 👋", fontSize = 24.sp)
    Text(if(en) "Make today count." else "আজকের দিনটি কাজে লাগাও।", color = Muted)
    Spacer(Modifier.height(20.dp))
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(20.dp)) {
            Text(if(en) "Today's progress" else "আজকের অগ্রগতি", color = Muted)
            Spacer(Modifier.height(8.dp))
            Text("$done / $total", fontSize = 34.sp, color = Mint)
            LinearProgressIndicator(progress = { if(total == 0) 0f else done.toFloat()/total }, modifier = Modifier.fillMaxWidth().padding(top=10.dp), color = Mint)
            Text(if(en) "Keep moving forward" else "এগিয়ে চলো", color = Muted, modifier = Modifier.padding(top=8.dp))
        }
    }
    Spacer(Modifier.height(14.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard(if(en) "Tasks" else "কাজ", total.toString(), Modifier.weight(1f))
        StatCard(if(en) "Habits" else "অভ্যাস", habits.toString(), Modifier.weight(1f))
    }
    Spacer(Modifier.height(16.dp))
    Button(onClick = openTasks, modifier = Modifier.fillMaxWidth()) { Text(if(en) "Open my tasks" else "আমার কাজ দেখুন") }
    Spacer(Modifier.height(12.dp))
    Text(if(en) "Your day, your pace." else "তোমার দিন, তোমার গতিতে।", color = Muted)
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) { Text(title, color = Muted); Text(value, fontSize = 27.sp, color = Accent) }
    }
}

@Composable
private fun ListScreen(en: Boolean, title: String, values: List<String>, kind: String, update: (List<String>) -> Unit) {
    var input by remember { mutableStateOf("") }
    Text(title, fontSize = 23.sp)
    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f),
            placeholder = { Text(if(en) "Add a $kind..." else "নতুন লেখা যোগ করুন...") }, singleLine = true)
        Spacer(Modifier.width(8.dp))
        Button(onClick = { if(input.isNotBlank()) { update(values + input.trim()); input = "" } }) { Icon(Icons.Default.Add, null) }
    }
    Spacer(Modifier.height(12.dp))
    if(values.isEmpty()) Text(if(en) "Nothing here yet. Add your first item!" else "এখনো কিছু নেই। প্রথম আইটেম যোগ করো!", color = Muted)
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(values) { index, item ->
            val checked = item.startsWith("✓")
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    if(kind == "task") Checkbox(checked, onCheckedChange = { yes ->
                        update(values.toMutableList().also { it[index] = (if(yes) "✓ " else "") + item.removePrefix("✓ ") })
                    })
                    Text(item.removePrefix("✓ "), modifier = Modifier.weight(1f), color = if(checked) Muted else Color.White)
                    IconButton(onClick = { update(values.toMutableList().also { it.removeAt(index) }) }) { Icon(Icons.Default.Delete, null, tint = Muted) }
                }
            }
        }
    }
}

@Composable
private fun FocusScreen(en: Boolean) {
    var seconds by remember { mutableIntStateOf(25 * 60) }
    var running by remember { mutableStateOf(false) }
    LaunchedEffect(running) {
        while(running && seconds > 0) { delay(1000); seconds-- }
        if(seconds == 0) running = false
    }
    val time = "%02d:%02d".format(seconds/60, seconds%60)
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(if(en) "Focus session" else "ফোকাস সেশন", color = Muted)
        Spacer(Modifier.height(20.dp))
        Text(time, fontSize = 58.sp, color = Accent)
        Text(if(en) "Pomodoro • 25 minutes" else "পোমোডোরো • ২৫ মিনিট", color = Muted)
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { running = !running }) { Text(if(running) if(en) "Pause" else "থামাও" else if(en) "Start" else "শুরু") }
            OutlinedButton(onClick = { running = false; seconds = 25*60 }) { Text(if(en) "Reset" else "রিসেট") }
        }
    }
}

private fun loadList(raw: String): List<String> = try {
    val a = JSONArray(raw); (0 until a.length()).map { a.getString(it) }
} catch (_: Exception) { emptyList() }

private fun saveList(items: List<String>): String = JSONArray().apply { items.forEach { put(it) } }.toString()
