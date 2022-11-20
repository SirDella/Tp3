package com.sirdella.tp2

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class fragment_detalle : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nombre = requireArguments().getString("nombre")
        val edad = requireArguments().getString("edad")
        val pais = requireArguments().getString("pais")
        val correo = requireArguments().getString("correo")
        val numero = requireArguments().getString("numero")
        val codigoPostal = requireArguments().getString("codigoPostal")
        val fotoGrande = requireArguments().getString("fotoGrande")

        val ivGrande = view.findViewById<ImageView>(R.id.imageViewDet)
        val tvNombre = view.findViewById<TextView>(R.id.textViewDetNombre)
        val tvEdad = view.findViewById<TextView>(R.id.textViewDetEdad)
        val tvPais = view.findViewById<TextView>(R.id.textViewDetPais)
        val tvCP = view.findViewById<TextView>(R.id.textViewDetCP)
        val tvCorreo = view.findViewById<TextView>(R.id.textViewDetCorreo)
        val tvNumero = view.findViewById<TextView>(R.id.textViewDetNum)

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
                    Toast.makeText(requireContext(), getString(R.string.str_intent_err_mail), Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(requireContext(), getString(R.string.str_intent_err_tel), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detalle, container, false)
    }
}