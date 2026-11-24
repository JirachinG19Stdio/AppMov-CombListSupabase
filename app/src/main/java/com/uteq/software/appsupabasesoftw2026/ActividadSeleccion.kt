package com.uteq.software.appsupabasesoftw2026

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.uteq.software.appsupabasesoftw2026.adapters.AlumnoAdapter
import com.uteq.software.appsupabasesoftw2026.data.SupabaseManager
import com.uteq.software.appsupabasesoftw2026.models.Alumno
import com.uteq.software.appsupabasesoftw2026.models.Materia
import com.uteq.software.appsupabasesoftw2026.utils.SupabaseErrorHandler
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch

class ActividadSeleccion : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_actividad_seleccion)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val listaSemestre = resources.getStringArray(R.array.niveles)
        val adapterNiveles = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            listaSemestre
        )
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.listaNiveles)
        autoCompleteTextView.setAdapter(adapterNiveles)

        val autoCompleteTextViewMaterias = findViewById<AutoCompleteTextView>(R.id.listaMaterias)

        autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            Toast.makeText(this, "Seleccionaste: $selectedItem", Toast.LENGTH_SHORT).show()

            val lstMaterias = ArrayList<String>()
            autoCompleteTextViewMaterias.setText("")
            autoCompleteTextViewMaterias.setAdapter(null)

            lifecycleScope.launch {
                try {
                    val listaMaterias = ArrayList(
                        SupabaseManager.client.from("materias")
                            .select {
                                filter { eq("nivel", position + 1) }  // position empieza en 0
                                order("nombre", Order.ASCENDING)
                            }
                            .decodeList<Materia>()
                    )
                    for (materia in listaMaterias) {
                        lstMaterias.add(materia.nombre ?: "")
                    }
                } catch (e: Exception) {
                    SupabaseErrorHandler.show(this@ActividadSeleccion, e)
                    lstMaterias.clear()
                } finally {
                    val adapterMaterias = ArrayAdapter(
                        this@ActividadSeleccion,
                        android.R.layout.simple_spinner_dropdown_item,
                        lstMaterias
                    )
                    autoCompleteTextViewMaterias.setAdapter(adapterMaterias)
                }
            }
        }


        val lvAlumnos = findViewById<ListView>(R.id.lvAlumnos)

        val header = layoutInflater.inflate(R.layout.ly_header, lvAlumnos, false)
        lvAlumnos.addHeaderView(header)

        lvAlumnos.setOnItemClickListener { parent, _, position, _ ->
            val alumno = parent.getItemAtPosition(position) as? Alumno
                ?: return@setOnItemClickListener
            Toast.makeText(this, "Item Seleccionado ${alumno.nombres}", Toast.LENGTH_SHORT).show()
        }

        cargarAlumnos(lvAlumnos)
    }

    private fun cargarAlumnos(lvAlumnos: ListView) {
        var lstAlumnos = ArrayList<Alumno>()
        lifecycleScope.launch {
            try {
                lstAlumnos = ArrayList(
                    SupabaseManager.client.from("alumnos")
                        .select { order("nombres", Order.ASCENDING) }
                        .decodeList<Alumno>()
                )
            } catch (e: Exception) {
                SupabaseErrorHandler.show(this@ActividadSeleccion, e)
            } finally {
                val adapter = AlumnoAdapter(this@ActividadSeleccion, lstAlumnos)
                lvAlumnos.adapter = adapter
            }
        }
    }
}