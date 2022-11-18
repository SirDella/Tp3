package com.sirdella.tp2

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions


class ActivityDetalle : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle2)

        val nombre = intent.getStringExtra("nombre")
        val edad = intent.getStringExtra("edad")
        val pais = intent.getStringExtra("pais")
        val correo = intent.getStringExtra("correo")
        val numero = intent.getStringExtra("numero")
        val codigoPostal = intent.getStringExtra("codigoPostal")
        val fotoGrande = intent.getStringExtra("fotoGrande")

        val ivGrande = findViewById<ImageView>(R.id.imageViewDet)
        val tvNombre = findViewById<TextView>(R.id.textViewDetNombre)
        val tvEdad = findViewById<TextView>(R.id.textViewDetEdad)
        val tvPais = findViewById<TextView>(R.id.textViewDetPais)
        val tvCP = findViewById<TextView>(R.id.textViewDetCP)
        val tvCorreo = findViewById<TextView>(R.id.textViewDetCorreo)
        val tvNumero = findViewById<TextView>(R.id.textViewDetNum)

        tvCorreo.setPaintFlags(tvCorreo.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
        tvNumero.setPaintFlags(tvNumero.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)

        Glide.with(this).load(fotoGrande).transition(DrawableTransitionOptions.withCrossFade()).centerCrop().into(ivGrande)
        tvNombre.text = nombre
        tvEdad.text = edad + " " + this.getString(R.string.anios_str)
        tvCP.text = getString(R.string.str_cp) + ": " + codigoPostal
        tvPais.text = getString(R.string.pais_str) + " " + pais
        tvCorreo.text = correo
        tvNumero.text = numero

        tvCorreo.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                val mIntent = Intent(Intent.ACTION_SEND)
                mIntent.data = Uri.parse("mailto:$correo")
                try{
                    startActivity(mIntent)
                }
                catch(e: Exception)
                {
                    Toast.makeText(this@ActivityDetalle, getString(R.string.str_intent_err_mail), Toast.LENGTH_SHORT).show()
                }
            }
        })

        tvNumero.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                val intent = Intent(Intent.ACTION_DIAL) //ACTION_CALL para llamar
                intent.data = Uri.parse("tel:$numero")
                try{
                    startActivity(intent)
                }
                catch(e: Exception)
                {
                    Toast.makeText(this@ActivityDetalle, getString(R.string.str_intent_err_tel), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}