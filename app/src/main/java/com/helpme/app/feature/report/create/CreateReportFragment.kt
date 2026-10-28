package com.helpme.app.feature.report.create

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.helpme.app.databinding.FragmentCreateReportBinding
import com.helpme.app.domain.model.OperationResult
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream

class CreateReportFragment : Fragment() {

    private var _binding: FragmentCreateReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateReportViewModel by viewModels()

    private var currentPhotoBase64: String = ""
    private var selectedTypeKey: String = ""
    private var selectedTypeId: String = ""

    private val incidentTypes = listOf(
        "Alumbrado Público" to "INC_ALUMBRADO",
        "Bacheo y Vías" to "INC_BACHEO",
        "Agua y Drenaje" to "INC_AGUA",
        "Basura y Limpieza" to "INC_BASURA",
        "Seguridad" to "INC_SEGURIDAD",
        "Otro" to "INC_OTRO"
    )

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { processImageUri(it) }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(
                requireContext(),
                "Permiso de ubicación denegado. Se pueden ingresar coordenadas manualmente.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdown()
        setupListeners()
        observeViewModel()
    }

    private fun setupDropdown() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            incidentTypes.map { it.first }
        )
        binding.actvReportType.setAdapter(adapter)

        binding.actvReportType.setOnItemClickListener { _, _, position, _ ->
            val selectedPair = incidentTypes[position]
            selectedTypeKey = selectedPair.first
            selectedTypeId = selectedPair.second
        }
    }

    private fun setupListeners() {
        binding.btnGetLocation.setOnClickListener {
            checkLocationPermissionAndFetch()
        }

        binding.btnPresetLocation.setOnClickListener {
            // Ubicación prederminada (Guatemala)
            binding.etLatitude.setText("14.6349")
            binding.etLongitude.setText("-90.5069")
            Toast.makeText(requireContext(), "Coordenadas fijadas a Guatemala", Toast.LENGTH_SHORT).show()
        }

        binding.btnPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnClearImage.setOnClickListener {
            currentPhotoBase64 = ""
            binding.ivReportPhoto.setImageResource(android.R.drawable.ic_menu_camera)
            Toast.makeText(requireContext(), "Imagen eliminada", Toast.LENGTH_SHORT).show()
        }

        binding.btnSubmitReport.setOnClickListener {
            val description = binding.etDescription.text.toString()
            val latText = binding.etLatitude.text.toString()
            val lonText = binding.etLongitude.text.toString()

            val latitude = latText.toDoubleOrNull() ?: 0.0
            val longitude = lonText.toDoubleOrNull() ?: 0.0

            viewModel.submitReport(
                tipo = selectedTypeKey,
                tipoId = selectedTypeId,
                descripcion = description,
                latitud = latitude,
                longitud = longitude,
                fotoBase64OrUrl = currentPhotoBase64
            )
        }
    }

    private fun checkLocationPermissionAndFetch() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(requireContext(), "Se necesita permiso de ubicación para obtener tu posición actual", Toast.LENGTH_SHORT).show()
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            Toast.makeText(requireContext(), "Servicio de ubicación no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        val lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (lastKnown != null) {
            binding.etLatitude.setText(lastKnown.latitude.toString())
            binding.etLongitude.setText(lastKnown.longitude.toString())
            Toast.makeText(requireContext(), "Ubicación GPS obtenida", Toast.LENGTH_SHORT).show()
        } else {
            // Intentar escuchar ubicación
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    binding.etLatitude.setText(location.latitude.toString())
                    binding.etLongitude.setText(location.longitude.toString())
                    Toast.makeText(requireContext(), "Ubicación GPS actualizada", Toast.LENGTH_SHORT).show()
                    locationManager.removeUpdates(this)
                }
            }
            try {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null)
            } catch (e: Exception) {
                binding.etLatitude.setText("14.6349")
                binding.etLongitude.setText("-90.5069")
                Toast.makeText(requireContext(), "Ubicación fijada por defecto (Guatemala)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processImageUri(uri: Uri) {
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                // Redimensionar imagen para optimizar espacio en Realtime Database
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, (400 * (bitmap.height.toDouble() / bitmap.width)).toInt(), true)
                val byteArrayOutputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                currentPhotoBase64 = "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)

                binding.ivReportPhoto.setImageBitmap(scaledBitmap)
                Toast.makeText(requireContext(), "Imagen procesada correctamente", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.createState.collect { state ->
                    when (state) {
                        is OperationResult.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnSubmitReport.isEnabled = false
                        }
                        is OperationResult.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnSubmitReport.isEnabled = true
                            Toast.makeText(requireContext(), "¡Reporte guardado exitosamente en Firebase!", Toast.LENGTH_LONG).show()
                            viewModel.resetState()
                            findNavController().popBackStack()
                        }
                        is OperationResult.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnSubmitReport.isEnabled = true
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                        is OperationResult.Idle -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnSubmitReport.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
