package com.sirdella.tp2

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings.Global.getString
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import kotlin.coroutines.coroutineContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //ViewModel:
        val viewmodel = ViewModelProvider(this).get(viewModel::class.java)

        //Fragments:
        if (supportFragmentManager.findFragmentById(R.id.contenedor) == null) mostrarLista()
    }

    fun mostrarLista(){
        val fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, fragment_lista::class.java.name)
        supportFragmentManager.beginTransaction().add(R.id.contenedor, fragment).commit()
    }

    fun mostrarDetalle(usuario: UsuarioDC){
        val fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, fragment_detalle::class.java.name)

        val arguments = Bundle()
        arguments.putString("nombre", usuario.nombre)
        arguments.putString("edad", usuario.edad)
        arguments.putString("pais", usuario.pais)
        arguments.putString("correo", usuario.correo)
        arguments.putString("numero", usuario.numero)
        arguments.putString("codigoPostal", usuario.codigoPostal)
        arguments.putString("fotoGrande", usuario.fotoGrande)

        fragment.arguments = arguments

        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in, R.anim.slide_out, R.anim.slide_in, R.anim.slide_out).add(R.id.contenedor, fragment).addToBackStack(null).commit()
    }
}

//region ViewModel ----------------------------------------------------------------------------------
class viewModel():ViewModel(){

    val repoUsuarios = claseServicioRug()

    private val _recargando = MutableLiveData<Boolean>()
    val recargando: LiveData<Boolean> = _recargando

    private val _listaUsuariosLD = MutableLiveData<ArrayList<UsuarioDC>>()
    val listaUsuariosLD: LiveData<ArrayList<UsuarioDC>> = _listaUsuariosLD

    init {
        actualizarUsuarios()
    }

    fun actualizarUsuarios(){
        _recargando.value = true
        repoUsuarios.obtenerUsuarios(callbackResultados = {
            _listaUsuariosLD.value = it
            _recargando.value = false
        })
    }
}
//endregion

//region Datos -------------------------------------------------------------------------------------------------
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

//region Cosas de Retrofit/Moshi ------------------------------------------------------------------------------

class claseServicioRug {
    private var servicioRug: ServicioRug

    init {
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
    }

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

    fun obtenerUsuarios(callbackResultados: (ArrayList<UsuarioDC>) -> Unit)
    {
        thread {
            Log.d("cosas", "buscando")
            val call = servicioRug.buscar("50")
            val listaUsuarios = ArrayList<UsuarioDC>()

            call.enqueue(object : Callback<ResultadoRug> {
                override fun onResponse(
                    call: Call<ResultadoRug>,
                    response: Response<ResultadoRug>
                ) {
                    val resultado = response.body()
                    val resultados = resultado?.results

                    if (resultados != null) {
                        for (usuario in resultados) {
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
                    //rvLista.scheduleLayoutAnimation()
                    //adapterUsuario.actualizarLista(listaUsuarios)
                    //refreshLayout.isRefreshing = false
                    callbackResultados(listaUsuarios)
                }

                override fun onFailure(call: Call<ResultadoRug>, t: Throwable) {
                    Log.d("Error", "Error: " + t.localizedMessage)
                }
            })
        }
    }
}

//endregion