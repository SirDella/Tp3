package com.sirdella.tp2

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.loader.content.Loader
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Recycler:
        rvLista = findViewById<RecyclerView>(R.id.lista)
        adapterUsuario = UsuarioAdapter(this)
        rvLista.adapter = adapterUsuario

        //Moshi:
        val moshi: Moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val conversorMoshi = MoshiConverterFactory.create(moshi).asLenient()

        //Retrofit:
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://randomuser.me/")
            .addConverterFactory(conversorMoshi)
            .build()
        servicioRug = retrofit.create(ServicioRug::class.java)

        //SwipeRefreshLayout:
        val refreshLayout = findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
        refreshLayout.setOnRefreshListener {
            thread{
                obtenerUsuarios(this)
            }
        }
        refreshLayout.isRefreshing = true

        thread {
            obtenerUsuarios(this)
        }
    }

    //region Cosas de Retrofit/Moshi ------------------------------------------------------------------------------

    private lateinit var servicioRug: ServicioRug

    interface ServicioRug {
        @GET("api")
        fun buscar(
            @Query("results")
            cantResultados: String
        ): Call<ResultadoRug>
    }

    data class ResultadoRug(
        val results: List<UsuarioRug>,
        val info: InfoRug
    )

    data class InfoRug(
        val seed: String,
        val results: Int,
        val page: Int,
        val version: String
    )

    data class UsuarioRug(
        val gender: String,
        val name: NameRug,
        val location: LocationRug,
        val email: String,
        val login: LoginRug,
        val dob: DobRug,
        val registered: RegisteredRug,
        val phone: String,
        val cell: String,
        val id: IdRug,
        val picture: PictureRug,
        val nat: String
    )

    data class NameRug(
        val title: String,
        val first: String,
        val last: String
    )

    data class LocationRug(
        val street: StreetRug,
        val city: String,
        val state: String,
        val country: String,
        val postcode: String,
        val coordinates: CoordinatesRug,
        val timezone: TimezoneRug
    )

    data class CoordinatesRug(
        val latitude: String,
        val longitude: String
    )

    data class TimezoneRug(
        val offset: String,
        val description: String
    )

    data class StreetRug(
        val number: Int,
        val name: String
    )

    data class LoginRug(
        val uuid: String,
        val username: String,
        val password: String,
        val salt: String,
        val md5: String,
        val sha1: String,
        val sha256: String
    )

    data class DobRug(
        val date: String,
        val age: Int
    )

    data class RegisteredRug(
        val date: String,
        val age: Int
    )

    data class IdRug(
        val name: String,
        val value: String?
    )

    data class PictureRug(
        val large: String,
        val medium: String,
        val thumbnail: String
    )

    fun obtenerUsuarios(contexto: Context){
        runOnUiThread { val refreshlayout = findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
            refreshlayout.setProgressBackgroundColorSchemeColor(Color.WHITE)
            refreshlayout.setColorSchemeColors(Color.BLACK) }
        val call = servicioRug.buscar(getString(R.string.str_cant_resultados))
        runOnUiThread {
            val refreshlayout = findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
            refreshlayout.setProgressBackgroundColorSchemeColor(Color.DKGRAY)
            refreshlayout.setColorSchemeColors(Color.WHITE) }

        call.enqueue(object: Callback<ResultadoRug> {
            override fun onResponse(call: Call<ResultadoRug>, response: Response<ResultadoRug>) {
                val resultado = response.body()
                val resultados = resultado?.results
                listaUsuarios = ArrayList()

                if (resultados != null)
                {
                    for(usuario in resultados)
                    {
                        val usuarioMapeo = UsuarioDC()
                        usuarioMapeo.codigoPostal = usuario.location.postcode.toString()
                        usuarioMapeo.correo = usuario.email
                        usuarioMapeo.edad = usuario.dob.age.toString()
                        usuarioMapeo.nombre = usuario.name.first + " " + usuario.name.last
                        usuarioMapeo.fotoChica = usuario.picture.medium
                        usuarioMapeo.fotoGrande = usuario.picture.large
                        usuarioMapeo.pais = usuario.location.country
                        usuarioMapeo.numero = usuario.phone
                        listaUsuarios.add(usuarioMapeo)
                    }
                }
                rvLista.scheduleLayoutAnimation()
                adapterUsuario.actualizarLista(listaUsuarios)
                val refreshLayout = findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
                refreshLayout.isRefreshing = false
            }
            override fun onFailure(call: Call<ResultadoRug>, t: Throwable) {
                Toast.makeText(contexto, "Error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                Log.e("Error", "Error: " + t.localizedMessage)
            }
        })
    }
    //endregion

    //region Datos -------------------------------------------------------------------------------------------------
    var listaUsuarios = ArrayList<UsuarioDC>()

    data class UsuarioDC(
        var fotoChica: String = "",
        var nombre: String = "",
        var edad: String = "",
        var pais: String = "",
        var correo: String = "",
        var numero: String = "",
        var codigoPostal: String = "",
        var fotoGrande: String = "",
    )

    //endregion

    //region Cosas del recycler view ---------------------------------------------------------------------------------
    lateinit var adapterUsuario: UsuarioAdapter
    lateinit var rvLista: RecyclerView

    class UsuarioAdapter(private val contexto: Context) : RecyclerView.Adapter<UsuarioVH>() {

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
                    val intent = Intent(contexto, ActivityDetalle::class.java)
                    intent.putExtra("nombre", usuario.nombre)
                    intent.putExtra("edad", usuario.edad)
                    intent.putExtra("pais", usuario.pais)
                    intent.putExtra("correo", usuario.correo)
                    intent.putExtra("numero", usuario.numero)
                    intent.putExtra("codigoPostal", usuario.codigoPostal)
                    intent.putExtra("fotoGrande", usuario.fotoGrande)
                    startActivity(contexto, intent, null)
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
