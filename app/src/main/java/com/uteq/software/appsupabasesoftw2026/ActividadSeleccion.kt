package com.uteq.software.appsupabasesoftw2026

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.textfield.MaterialAutoCompleteTextView
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
        // Íconos de la barra de estado en blanco (la cabecera es oscura)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContentView(R.layout.activity_actividad_seleccion)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ---------- Paso 4: capturar la selección de semestre ----------
        // Los items los carga app:simpleItems="@array/niveles" desde el XML
        val listaNiveles = findViewById<MaterialAutoCompleteTextView>(R.id.listaNiveles)
        listaNiveles.setOnItemClickListener { _, _, position, _ ->
            cargarMaterias(position + 1)   // position empieza en 0
        }

        // ---------- Paso 11: encabezado y evento click ----------
        val lvAlumnos = findViewById<ListView>(R.id.lvAlumnos)
        val header = layoutInflater.inflate(R.layout.ly_header, lvAlumnos, false)
        lvAlumnos.addHeaderView(header, null, false)   // false = el encabezado no se puede tocar

        lvAlumnos.setOnItemClickListener { parent, _, position, _ ->
            val alumno = parent.getItemAtPosition(position) as? Map<*, *>
                ?: return@setOnItemClickListener
            Toast.makeText(
                this, "Item seleccionado: ${alumno["nombres"]}",
                Toast.LENGTH_SHORT
            ).show()
        }

        cargarAlumnos()
    }

    // ---------- Paso 7 (9.1): materias filtradas por nivel ----------
    private fun cargarMaterias(nivel: Int) {
        lifecycleScope.launch {
            val selector = findViewById<MaterialAutoCompleteTextView>(R.id.listaMaterias)
            selector.setText("")
            selector.setSimpleItems(emptyArray<String>())
            try {
                val materias = SupabaseManager.client.from("materias")
                    .select {
                        filter { eq("nivel", nivel) }
                        order("nombre", Order.ASCENDING)
                    }
                    .decodeList<Materia>()

                selector.setSimpleItems(
                    materias.map { it.nombre.orEmpty() }.toTypedArray()
                )
            } catch (e: Exception) {
                SupabaseErrorHandler.show(this@ActividadSeleccion, e)
            }
        }
    }

    // ---------- Paso 10 (12.2): alumnos con SimpleAdapter + foto con Glide ----------
    private fun cargarAlumnos() {
        lifecycleScope.launch {
            try {
                val alumnos = SupabaseManager.client.from("alumnos")
                    .select { order("nombres", Order.ASCENDING) }
                    .decodeList<Alumno>()

                // Cada alumno se convierte en un mapa clave -> valor
                val datos = alumnos.map {
                    mapOf(
                        "id" to it.id,
                        "foto" to it.foto.orEmpty(),
                        "nombres" to it.nombres.orEmpty(),
                        "correo" to it.correo.orEmpty(),
                        "telefono" to it.telefono.orEmpty()
                    )
                }
                // Las claves del mapa (from) se ponen en las vistas (to), en el mismo orden
                val adapter = SimpleAdapter(
                    this@ActividadSeleccion, datos, R.layout.item_alumno,
                    arrayOf("foto", "nombres", "correo", "telefono"),
                    intArrayOf(R.id.imgFoto, R.id.txtNombre, R.id.txtCorreo, R.id.txtTelefono)
                )

                // SimpleAdapter no sabe cargar imágenes desde una URL:
                // el ViewBinder le dice que, si la vista es un ImageView, use Glide
                adapter.setViewBinder { view, data, _ ->
                    if (view is ImageView) {
                        Glide.with(view)
                            .load(data as? String)
                            .placeholder(R.drawable.ic_avatar)   // silueta mientras carga
                            .error(R.drawable.ic_avatar)         // silueta si falla o no hay foto
                            .circleCrop()                                      // la recorta en círculo
                            .into(view)
                        true    // true = "ya lo manejé yo"
                    } else {
                        false   // false = los TextView los llena SimpleAdapter normal
                    }
                }

                findViewById<ListView>(R.id.lvAlumnos).adapter = adapter
            } catch (e: Exception) {
                SupabaseErrorHandler.show(this@ActividadSeleccion, e)
            }
        }
    }
}
