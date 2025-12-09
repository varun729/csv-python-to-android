package net.vagrawal.android.csv_python_to_android.ui.transform

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import net.vagrawal.android.csv_python_to_android.databinding.FragmentTransformBinding
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class TransformFragment : Fragment() {

    private var _binding: FragmentTransformBinding? = null
    private val binding get() = _binding!!

    private val openCsvLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let { handleCsvUri(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransformBinding.inflate(inflater, container, false)
        val root = binding.root

        binding.buttonSelectCsv.setOnClickListener {
            pickCsv()
        }

        return root
    }

    private fun pickCsv() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/csv", "text/comma-separated-values", "application/vnd.ms-excel"))
        }
        openCsvLauncher.launch(intent)
    }

    private fun handleCsvUri(uri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        binding.textSummary.text = "Processing..."

        // Copy to a local cache file
        val context = requireContext()
        val cacheDir = context.cacheDir
        val fileName = queryFileName(uri) ?: "input.csv"
        val dstFile = File(cacheDir, fileName)
        copyUriToFile(uri, dstFile)

        // Ensure Python is started
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }

        // Run Python on a background thread
        Thread {
            try {
                val py = Python.getInstance()
                val module = py.getModule("process_csv")
                val result: PyObject = module.callAttr("process_csv", dstFile.absolutePath, cacheDir.absolutePath)

                val summary = result.callAttr("get", "summary").toString()
                val plotPath = result.callAttr("get", "plot_path").toString()

                requireActivity().runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    binding.textSummary.text = summary
                    val bmp = BitmapFactory.decodeFile(plotPath)
                    binding.imagePlot.setImageBitmap(bmp)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    binding.textSummary.text = "Error: ${e.message}"
                }
            }
        }.start()
    }

    private fun copyUriToFile(uri: Uri, dstFile: File) {
        val resolver: ContentResolver = requireContext().contentResolver
        resolver.openInputStream(uri)?.use { input: InputStream ->
            FileOutputStream(dstFile).use { out ->
                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                while (true) {
                    bytesRead = input.read(buffer)
                    if (bytesRead == -1) break
                    out.write(buffer, 0, bytesRead)
                }
                out.flush()
            }
        }
    }

    private fun queryFileName(uri: Uri): String? {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) {
                return it.getString(nameIndex)
            }
        }
        return null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}