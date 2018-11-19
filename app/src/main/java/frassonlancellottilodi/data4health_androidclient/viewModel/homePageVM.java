package frassonlancellottilodi.data4health_androidclient.viewModel;

import android.arch.lifecycle.ViewModel;

public class homePageVM extends ViewModel {

    public String testData = "test";

    public String getTestData() {
        return testData;
    }

    public void setTestData(String testData) {
        this.testData = testData;
    }
}
