package com.sirdella.tp2

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import java.util.*


class fragment_lista : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //ViewModel:
        val viewmodel = ViewModelProvider(this).get(viewModel::class.java)
        viewmodel.listaUsuariosLD.observe(viewLifecycleOwner){ usuarios->
            rvLista.scheduleLayoutAnimation()
            adapterUsuario.actualizarLista(usuarios)
        }

        //Recycler:
        rvLista = view.findViewById<RecyclerView>(R.id.lista)
        adapterUsuario = UsuarioAdapter(this.requireContext(), callbackClick = {
            (activity as MainActivity).mostrarDetalle(it)
            ocultarTeclado()

        })
        rvLista.adapter = adapterUsuario



        //EditText:
        val etFiltro = view.findViewById<EditText>(R.id.EditTextFiltro)
        etFiltro.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapterUsuario.actualizarLista(filtrar(etFiltro.text.toString(), viewmodel.listaUsuariosLD.value))
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        //SwipeRefreshLayout:
        val refreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
        refreshLayout.setOnRefreshListener {
            viewmodel.actualizarUsuarios()
        }
        viewmodel.recargando.observe(viewLifecycleOwner){
            refreshLayout.isRefreshing = it
        }
    }

    private fun filtrar(busqueda: String, lista: ArrayList<UsuarioDC>?): ArrayList<UsuarioDC> {
        val salida = ArrayList<UsuarioDC>()
        if (lista == null) return salida
        for(i in lista)
        {
            if (i.nombre.uppercase().contains(busqueda.uppercase()))
            {
                salida.add(i)
            }
        }
        return salida
    }

    private fun ocultarTeclado() {
        val imManager =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        val windowToken = activity?.currentFocus?.windowToken
        if (windowToken != null) {
            imManager?.hideSoftInputFromWindow(windowToken, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lista, container, false)
    }

    //region Cosas del recycler view ------------------------------------------------------------------------

    lateinit var adapterUsuario: UsuarioAdapter
    lateinit var rvLista: RecyclerView

    class UsuarioAdapter(private val contexto: Context, private val callbackClick: (UsuarioDC) -> Unit) : RecyclerView.Adapter<UsuarioVH>() {

        var usuarios = listOf<UsuarioDC>()

        fun actualizarLista(nuevaLista: List<UsuarioDC>){
            usuarios = nuevaLista
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioVH {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_lista, parent, false)
            return UsuarioVH(view)
        }

        override fun onBindViewHolder(holder: UsuarioVH, position: Int) {
            val usuario = usuarios[position]

            holder.tvNombre.text = usuario.nombre
            holder.tvEdad.text = usuario.edad + " " + contexto.getString(R.string.anios_str)
            holder.tvPais.text = contexto.getString(R.string.pais_str) + " " + usuario.pais
            Glide.with(contexto).load(usuario.fotoChica).transition(DrawableTransitionOptions.withCrossFade()).centerCrop().into(holder.ivImagen)

            holder.itemView.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    callbackClick(usuario)
                }
            })
        }

        override fun getItemCount(): Int {
            return usuarios.size
        }
    }

    class UsuarioVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.textViewNombre)
        val tvPais: TextView = itemView.findViewById(R.id.textViewPais)
        val tvEdad = itemView.findViewById<TextView>(R.id.textViewEdad)
        val ivImagen = itemView.findViewById<ImageView>(R.id.imageView)
    }

    //endregion
}

