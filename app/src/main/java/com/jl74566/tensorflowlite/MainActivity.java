package com.jl74566.tensorflowlite;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import com.jl74566.tensorflowlite.databinding.ActivityMainBinding;

public class MainActivity extends BaseActivity {
    private MainViewModel mMainViewModel;
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mMainViewModel = new ViewModelProvider(getViewModelStore(), viewModelFactory).get(MainViewModel.class);

        mBinding.setViewModel(mMainViewModel);
    }
}
