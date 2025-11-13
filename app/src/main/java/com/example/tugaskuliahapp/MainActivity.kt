package com.example.tugaskuliahapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.example.tugaskuliahapp.data.AppDatabase
import com.example.tugaskuliahapp.data.Tugas
import com.example.tugaskuliahapp.ui.theme.TugasKuliahAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "tugas-db").build()
        val dao = db.tugasDao()

        @OptIn(ExperimentalMaterial3Api::class)
        setContent {
            TugasKuliahAppTheme {
                var tugasList by remember { mutableStateOf(emptyList<Tugas>()) }
                var showDialog by remember { mutableStateOf(false) }
                var tugasYangDiedit by remember { mutableStateOf<Tugas?>(null) }

                var statusFilter by remember { mutableStateOf("Semua") }
                var mataKuliahFilter by remember { mutableStateOf("Semua") }

                var tahunDari by remember { mutableStateOf("") }
                var bulanDari by remember { mutableStateOf("") }
                var hariDari by remember { mutableStateOf("") }
                var tahunSampai by remember { mutableStateOf("") }
                var bulanSampai by remember { mutableStateOf("") }
                var hariSampai by remember { mutableStateOf("") }

                LaunchedEffect(Unit) {
                    tugasList = dao.getAll()
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    "Tugas Kuliah 📚",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF1565C0)
                            )
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showDialog = true }) {
                            Text("+", fontSize = 24.sp)
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE3F2FD))
                            .padding(innerPadding)
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {

                            // Statistik tugas
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFBBDEFB)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                val total = tugasList.size
                                val belum = tugasList.count { it.status.equals("Belum", true) }
                                val proses = tugasList.count { it.status.equals("Proses", true) }
                                val selesai = tugasList.count { it.status.equals("Selesai", true) }

                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    StatistikBox("Total", total, Color(0xFF0D47A1))
                                    StatistikBox("Belum", belum, Color.Red)
                                    StatistikBox("Proses", proses, Color(0xFFFFA000))
                                    StatistikBox("Selesai", selesai, Color(0xFF2E7D32))
                                }
                            }

                            // Filter Section
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Filter Tugas", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    FilterDropdown("Status", listOf("Semua", "Belum", "Proses", "Selesai"), statusFilter) {
                                        statusFilter = it
                                    }

                                    FilterDropdown(
                                        "Mata Kuliah",
                                        listOf("Semua") + tugasList.map { it.mataKuliah }.distinct(),
                                        mataKuliahFilter
                                    ) {
                                        mataKuliahFilter = it
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Tanggal Dari", fontWeight = FontWeight.Bold, color = Color.Black)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(value = tahunDari, onValueChange = { tahunDari = it }, label = { Text("Tahun") }, modifier = Modifier.weight(1f))
                                        OutlinedTextField(value = bulanDari, onValueChange = { bulanDari = it }, label = { Text("Bulan") }, modifier = Modifier.weight(1f))
                                        OutlinedTextField(value = hariDari, onValueChange = { hariDari = it }, label = { Text("Hari") }, modifier = Modifier.weight(1f))
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Tanggal Sampai", fontWeight = FontWeight.Bold, color = Color.Black)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(value = tahunSampai, onValueChange = { tahunSampai = it }, label = { Text("Tahun") }, modifier = Modifier.weight(1f))
                                        OutlinedTextField(value = bulanSampai, onValueChange = { bulanSampai = it }, label = { Text("Bulan") }, modifier = Modifier.weight(1f))
                                        OutlinedTextField(value = hariSampai, onValueChange = { hariSampai = it }, label = { Text("Hari") }, modifier = Modifier.weight(1f))
                                    }
                                }
                            }

                            val tugasTerfilter = tugasList.filter { tugas ->
                                val cocokStatus = statusFilter == "Semua" || tugas.status.equals(statusFilter, true)
                                val cocokMK = mataKuliahFilter == "Semua" || tugas.mataKuliah == mataKuliahFilter

                                val dariStr = listOfNotNull(
                                    tahunDari.takeIf { it.isNotBlank() },
                                    bulanDari.takeIf { it.isNotBlank() },
                                    hariDari.takeIf { it.isNotBlank() }
                                ).joinToString("-")

                                val sampaiStr = listOfNotNull(
                                    tahunSampai.takeIf { it.isNotBlank() },
                                    bulanSampai.takeIf { it.isNotBlank() },
                                    hariSampai.takeIf { it.isNotBlank() }
                                ).joinToString("-")

                                val cocokTanggal = try {
                                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val dari = if (dariStr.length >= 8) sdf.parse(dariStr)?.time else null
                                    val sampai = if (sampaiStr.length >= 8) sdf.parse(sampaiStr)?.time else null
                                    val deadline = tugas.deadline
                                    (dari == null || deadline >= dari) && (sampai == null || deadline <= sampai)
                                } catch (e: Exception) {
                                    true
                                }

                                cocokStatus && cocokMK && cocokTanggal
                            }

                            // List Tugas
                            TugasListScreen(
                                tugasList = tugasTerfilter,
                                onEdit = { tugasYangDiedit = it },
                                onDelete = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        dao.delete(it)
                                        tugasList = dao.getAll()
                                    }
                                }
                            )
                        }

                        if (showDialog) {
                            InputTugasDialog(
                                onDismiss = { showDialog = false },
                                onSave = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        dao.insert(it)
                                        tugasList = dao.getAll()
                                    }
                                    showDialog = false
                                }
                            )
                        }

                        if (tugasYangDiedit != null) {
                            InputTugasDialog(
                                tugasAwal = tugasYangDiedit,
                                onDismiss = { tugasYangDiedit = null },
                                onSave = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        dao.update(it)
                                        tugasList = dao.getAll()
                                    }
                                    tugasYangDiedit = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatistikBox(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
        Text(value.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
    }
}

@Composable
fun FilterDropdown(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$label:", modifier = Modifier.width(100.dp), color = Color.Black)
        Box {
            Button(onClick = { expanded = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))) {
                Text(selected, color = Color.White)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = {
                        onSelect(it)
                        expanded = false
                    })
                }
            }
        }
    }
}

@Composable
fun TugasListScreen(
    tugasList: List<Tugas>,
    onEdit: (Tugas) -> Unit,
    onDelete: (Tugas) -> Unit
) {
    val now = System.currentTimeMillis()
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    LazyColumn(modifier = Modifier.padding(8.dp)) {
        items(tugasList) { tugas ->
            val terlambat = now > tugas.deadline && !tugas.status.equals("Selesai", true)
            val warnaStatus = when {
                tugas.status.equals("Selesai", true) -> Color(0xFF2E7D32)
                terlambat -> Color.Red
                else -> Color.Transparent
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tugas.mataKuliah,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF0D47A1)
                        )

                        if (tugas.status.equals("Selesai", true)) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF2E7D32), shape = MaterialTheme.shapes.small)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Selesai ✅", color = Color.White, fontSize = 12.sp)
                            }
                        } else if (terlambat) {
                            Box(
                                modifier = Modifier
                                    .background(Color.Red, shape = MaterialTheme.shapes.small)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Terlambat ⏰", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    Text(text = tugas.deskripsi, style = MaterialTheme.typography.bodyMedium, color = Color.Black)
                    Text(text = "Deadline: ${formatter.format(tugas.deadline)}", fontSize = 13.sp, color = Color.Black)
                    Text(text = "Status: ${tugas.status}", fontSize = 13.sp, color = Color.Black)
                    Text(text = "Catatan: ${tugas.catatan}", fontSize = 13.sp, color = Color.Black)

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onEdit(tugas) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))) {
                            Text("✏️ Edit", color = Color.White)
                        }
                        Button(
                            onClick = { onDelete(tugas) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCDD2))
                        ) {
                            Text("🗑️ Hapus", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InputTugasDialog(
    tugasAwal: Tugas? = null,
    onDismiss: () -> Unit,
    onSave: (Tugas) -> Unit
) {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    var mataKuliah by remember { mutableStateOf(tugasAwal?.mataKuliah ?: "") }
    var deskripsi by remember { mutableStateOf(tugasAwal?.deskripsi ?: "") }
    var status by remember { mutableStateOf(tugasAwal?.status ?: "Belum") }
    var tahun by remember { mutableStateOf(tugasAwal?.let { formatter.format(it.deadline).substring(0, 4) } ?: "") }
    var bulan by remember { mutableStateOf(tugasAwal?.let { formatter.format(it.deadline).substring(5, 7) } ?: "") }
    var hari by remember { mutableStateOf(tugasAwal?.let { formatter.format(it.deadline).substring(8, 10) } ?: "") }
    var jam by remember { mutableStateOf(tugasAwal?.let { formatter.format(it.deadline).substring(11, 16) } ?: "") }
    var catatan by remember { mutableStateOf(tugasAwal?.catatan ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val tanggalGabung = "$tahun-$bulan-$hari $jam"
                val deadlineMillis = try {
                    formatter.parse(tanggalGabung)?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }

                val tugas = Tugas(
                    id = tugasAwal?.id ?: 0,
                    mataKuliah = mataKuliah,
                    deskripsi = deskripsi,
                    deadline = deadlineMillis,
                    status = status,
                    catatan = catatan
                )
                onSave(tugas)
            }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        title = {
            Text(if (tugasAwal == null) "Tambah Tugas" else "Edit Tugas", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = mataKuliah, onValueChange = { mataKuliah = it }, label = { Text("Mata Kuliah") })
                OutlinedTextField(value = deskripsi, onValueChange = { deskripsi = it }, label = { Text("Deskripsi") })
                OutlinedTextField(value = status, onValueChange = { status = it }, label = { Text("Status") })

                Text("Tanggal Deadline", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = tahun, onValueChange = { tahun = it }, label = { Text("Tahun") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = bulan, onValueChange = { bulan = it }, label = { Text("Bulan") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = hari, onValueChange = { hari = it }, label = { Text("Hari") }, modifier = Modifier.weight(1f))
                }

                OutlinedTextField(value = jam, onValueChange = { jam = it }, label = { Text("Jam (HH:mm)") })
                OutlinedTextField(value = catatan, onValueChange = { catatan = it }, label = { Text("Catatan") })
            }
        }
    )
}
