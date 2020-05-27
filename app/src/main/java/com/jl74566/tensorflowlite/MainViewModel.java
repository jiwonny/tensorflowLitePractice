package com.jl74566.tensorflowlite;

import android.app.Application;
import android.util.Log;

import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    public ObservableField<String> writeCommand;

    private ModelRepository mModelRepository;

    public MainViewModel() {
        writeCommand = new ObservableField<>("");
        mModelRepository = new ModelRepository(ApplicationClass.getApplicationClassContext(), "");
        mModelRepository.loadModels();
    }

    public void btnClicked() {
        Log.d("write", writeCommand.get());
        mModelRepository.setInput(writeCommand.get());
        mModelRepository.doClassify();
    }


}
