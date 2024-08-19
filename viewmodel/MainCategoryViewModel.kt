package com.example.hammami.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.data.Service
import com.example.hammami.util.Resource
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "MainCategoryViewModel"
@HiltViewModel
class MainCategoryViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
): ViewModel() {

    private val _newServices = MutableStateFlow<Resource<List<Service>>>(Resource.Loading())
    val newServices: StateFlow<Resource<List<Service>>> = _newServices

    private val _bestDeals = MutableStateFlow<Resource<List<Service>>>(Resource.Loading())
    val bestDeals: StateFlow<Resource<List<Service>>> = _bestDeals

    private val _recommended = MutableStateFlow<Resource<List<Service>>>(Resource.Loading())
    val recommended: StateFlow<Resource<List<Service>>> = _recommended

    init {
        fetchNewServices()
        fetchBestDeals()
        fetchRecommended()
    }

    fun fetchNewServices() {
        viewModelScope.launch {
            val allServices = mutableListOf<Service>()

            try {
                val esteticaTrattamentoCorpoSnapshot = firestore.collection("/Servizi/Estetica/Trattamento corpo")
                    .whereEqualTo("Sezione homepage", "Novità").get().await()
                allServices.addAll(esteticaTrattamentoCorpoSnapshot.toObjects(Service::class.java))

                val esteticaEpilazioneSnapshot = firestore.collection("/Servizi/Estetica/Epilazione corpo con cera")
                    .whereEqualTo("Sezione homepage", "Novità").get().await()
                allServices.addAll(esteticaEpilazioneSnapshot.toObjects(Service::class.java))

                val esteticaTrattamentoVisoSnaphot = firestore.collection("/Servizi/Estetica/Trattamento viso")
                    .whereEqualTo("Sezione homepage", "Novità").get().await()
                allServices.addAll(esteticaTrattamentoVisoSnaphot.toObjects(Service::class.java))

                val benessereSnapshot = firestore.collection("/Servizi/Benessere/trattamenti")
                    .whereEqualTo("Sezione homepage", "Novità").get().await()
                allServices.addAll(benessereSnapshot.toObjects(Service::class.java))

                val massaggiSnaphot = firestore.collection("/Servizi/Massaggi/trattamenti")
                    .whereEqualTo("Sezione homepage", "Novità").get().await()
                allServices.addAll(massaggiSnaphot.toObjects(Service::class.java))

                _newServices.emit(Resource.Success(allServices))
            } catch (e: Exception) {
                Log.e(TAG, "Errore nel recupero dei nuovi servizi: ${e.message}", e)
                _newServices.emit(Resource.Error(e.message ?: "Si è verificato un errore sconosciuto"))
            }
        }
    }

    fun fetchBestDeals() {
        viewModelScope.launch {
            val allServices = mutableListOf<Service>()

            try {
                val esteticaTrattamentoCorpoSnapshot = firestore.collection("/Servizi/Estetica/Trattamento corpo")
                    .whereEqualTo("Sezione homepage", "Offerte").get().await()
                allServices.addAll(esteticaTrattamentoCorpoSnapshot.toObjects(Service::class.java))

                val esteticaEpilazioneSnapshot = firestore.collection("/Servizi/Estetica/Epilazione corpo con cera")
                    .whereEqualTo("Sezione homepage", "Offerte").get().await()
                allServices.addAll(esteticaEpilazioneSnapshot.toObjects(Service::class.java))

                val esteticaTrattamentoVisoSnaphot = firestore.collection("/Servizi/Estetica/Trattamento viso")
                    .whereEqualTo("Sezione homepage", "Offerte").get().await()
                allServices.addAll(esteticaTrattamentoVisoSnaphot.toObjects(Service::class.java))

                val benessereSnapshot = firestore.collection("/Servizi/Benessere/trattamenti")
                    .whereEqualTo("Sezione homepage", "Offerte").get().await()
                allServices.addAll(benessereSnapshot.toObjects(Service::class.java))

                val massaggiSnaphot = firestore.collection("/Servizi/Massaggi/trattamenti")
                    .whereEqualTo("Sezione homepage", "Offerte").get().await()
                allServices.addAll(massaggiSnaphot.toObjects(Service::class.java))

                _bestDeals.emit(Resource.Success(allServices))
            } catch (e: Exception) {
                Log.e(TAG, "Errore nel recupero dei nuovi servizi: ${e.message}", e)
                _bestDeals.emit(Resource.Error(e.message ?: "Si è verificato un errore sconosciuto"))
            }
        }
    }

    fun fetchRecommended() {
        viewModelScope.launch {
            val allServices = mutableListOf<Service>()

            try {
                val esteticaTrattamentoCorpoSnapshot = firestore.collection("/Servizi/Estetica/Trattamento corpo")
                    .whereEqualTo("Sezione homepage", "Consigliati").get().await()
                allServices.addAll(esteticaTrattamentoCorpoSnapshot.toObjects(Service::class.java))

                val esteticaEpilazioneSnapshot = firestore.collection("/Servizi/Estetica/Epilazione corpo con cera")
                    .whereEqualTo("Sezione homepage", "Consigliati").get().await()
                allServices.addAll(esteticaEpilazioneSnapshot.toObjects(Service::class.java))

                val esteticaTrattamentoVisoSnaphot = firestore.collection("/Servizi/Estetica/Trattamento viso")
                    .whereEqualTo("Sezione homepage", "Consigliati").get().await()
                allServices.addAll(esteticaTrattamentoVisoSnaphot.toObjects(Service::class.java))

                val benessereSnapshot = firestore.collection("/Servizi/Benessere/trattamenti")
                    .whereEqualTo("Sezione homepage", "Consigliati").get().await()
                allServices.addAll(benessereSnapshot.toObjects(Service::class.java))

                val massaggiSnaphot = firestore.collection("/Servizi/Massaggi/trattamenti")
                    .whereEqualTo("Sezione homepage", "Consigliati").get().await()
                allServices.addAll(massaggiSnaphot.toObjects(Service::class.java))

                _recommended.emit(Resource.Success(allServices))
            } catch (e: Exception) {
                Log.e(TAG, "Errore nel recupero dei nuovi servizi: ${e.message}", e)
                _recommended.emit(Resource.Error(e.message ?: "Si è verificato un errore sconosciuto"))
            }
        }
    }
}