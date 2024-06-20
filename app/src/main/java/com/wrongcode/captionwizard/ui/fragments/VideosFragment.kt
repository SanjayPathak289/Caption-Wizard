package com.wrongcode.captionwizard.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.wrongcode.captionwizard.R
import com.wrongcode.captionwizard.adapters.DraftVideosAdapter
import com.wrongcode.captionwizard.databinding.FragmentVideosBinding
import com.wrongcode.captionwizard.viewmodels.SubtitleViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideosFragment : Fragment() {
    private var _binding : FragmentVideosBinding ?= null
    private val binding get() = _binding!!
    private val subtitleViewModel by viewModels<SubtitleViewModel>()
    private val adapter : DraftVideosAdapter by lazy { DraftVideosAdapter(requireContext(), subtitleViewModel) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentVideosBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.subtitleViewModel = subtitleViewModel
        binding.adapter = adapter
        binding.draftRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.draftRecyclerView.adapter = adapter
        subtitleViewModel.readAllSubtitle.observe(viewLifecycleOwner){
            binding.videoProgressBar.visibility = View.INVISIBLE
            if(it.isEmpty()){
                binding.draftImageView.visibility = View.VISIBLE
                binding.draftTextView.visibility = View.VISIBLE
                binding.draftRecyclerView.visibility = View.INVISIBLE
            }
//            if(it.isNotEmpty()){
//                binding.draftImageView.visibility = View.GONE
//                binding.draftTextView.visibility = View.GONE
//                binding.draftRecyclerView.visibility = View.GONE
//            }
        }
        binding.draftRecyclerView.layoutManager = GridLayoutManager(requireContext(),2)
        binding.draftRecyclerView.adapter = adapter
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}