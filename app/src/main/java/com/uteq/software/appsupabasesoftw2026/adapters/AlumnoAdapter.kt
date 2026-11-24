package com.uteq.software.appsupabasesoftw2026.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.uteq.software.appsupabasesoftw2026.R
import com.uteq.software.appsupabasesoftw2026.models.Alumno

class AlumnoAdapter(context: Context, var alumnos: ArrayList<Alumno>) :
    ArrayAdapter<Alumno>(context, R.layout.item_alumno, alumnos) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_alumno, parent, false)

        val alumno = alumnos[position]
        val txtNombre = view.findViewById<TextView>(R.id.txtNombre)
        val txtCorreo = view.findViewById<TextView>(R.id.txtCorreo)
        val txtTelefono = view.findViewById<TextView>(R.id.txtTelefono)

        txtNombre.text = alumno.nombres
        txtCorreo.text = alumno.correo
        txtTelefono.text = alumno.telefono
        return view
    }
}