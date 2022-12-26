package com.beerus.flow_code_learn.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.beerus.flow_code_learn.MyApplication.Companion.TAG
import com.beerus.flow_code_learn.R
import com.beerus.flow_code_learn.adapters.PlantAdapter
import com.beerus.flow_code_learn.databinding.FragmentPlantListBinding
import com.beerus.flow_code_learn.models.Plant
import com.beerus.flow_code_learn.utils.Injector
import com.beerus.flow_code_learn.viewmodels.PlantListViewModel
import com.google.android.material.snackbar.Snackbar

class PlantListFragment : Fragment() {
    private var binding: FragmentPlantListBinding? = null
    private lateinit var safeContext: Context
    private val adapter by lazy { PlantAdapter() }
    private val viewModel: PlantListViewModel by viewModels {
        Injector.providePlantListViewModelFactory(safeContext)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlantListBinding.inflate(inflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerview()
        initObserver()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_plant_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.filter_zone -> {
                updateData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initRecyclerview() {
        binding?.apply {
            plantList.adapter = adapter
        }
        setHasOptionsMenu(true)
    }

    private fun initObserver() {
        binding?.apply {
            viewModel.spinner.observe(viewLifecycleOwner) { isShow ->
                spinner.isVisible = isShow
            }

            viewModel.snackBar.observe(viewLifecycleOwner) { text ->
                Snackbar.make(root, text ?: "", Snackbar.LENGTH_SHORT).show()
                viewModel.onSnackBarShown()
            }

            viewModel.plants.observe(viewLifecycleOwner) {
                Log.d(TAG, "initObserver: listplant")
                subscribeUi(it)
            }
        }
    }

    private fun subscribeUi(plants: List<Plant>?) {
        adapter.submitList(plants)
    }

    private fun updateData() {
        with(viewModel) {
            if (isFiltered()) {
                clearGrowZoneNumber()
            } else {
                setGrowZoneNumber(9)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}