package com.xbot.vktest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.xbot.vktest.databinding.FragmentClockBinding

class ClockFragment : Fragment() {

    private var _binding: FragmentClockBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClockBinding.inflate(inflater, container, false)

        val timeZones = resources.getStringArray(R.array.time_zones)
        adapter = ArrayAdapter(requireContext(), R.layout.item_drop_down, timeZones)

        binding.applyButton.setOnClickListener {
            val timeZone = binding.autoCompleteText.text.toString()
            binding.clockView.timeZone = timeZone
        }

        val itemPosition = adapter.getPosition(binding.clockView.timeZone).takeIf { it != -1 } ?: 0
        val defaultText = adapter.getItem(itemPosition)
        binding.autoCompleteText.setText(defaultText)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.autoCompleteText.setAdapter(adapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}