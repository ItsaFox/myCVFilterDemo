package d.blueshoestring.mycvdemo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import d.blueshoestring.mycvdemo.CVEntry;
import d.blueshoestring.mycvdemo.R;

public class EntryDialog extends DialogFragment implements View.OnClickListener {
    public Button BackButton;
    public TextView TitleTextField, WhereField, BeginningText, EndText, LocationText, Resp0Text, RespText1, RespText2, RespText3;
    public ConstraintLayout layout;
    public String version;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.alertdialog_entry_dialog, container, false);

        assert getArguments() != null;
        CVEntry entry = getArguments().getParcelable("Entry");
        String version = getArguments().getString("Version");

        layout = view.findViewById(R.id.dialogLayout);
        TitleTextField = view.findViewById(R.id.TitleTextField);
        LocationText = view.findViewById(R.id.LocationText);
        WhereField = view.findViewById(R.id.WhereField);
        BeginningText = view.findViewById(R.id.BeginningText);
        EndText = view.findViewById(R.id.EndText);
        Resp0Text = view.findViewById(R.id.Resp0Text);
        RespText1 = view.findViewById(R.id.Resp1Text);
        RespText2 = view.findViewById(R.id.Resp2Text);
        RespText3 = view.findViewById(R.id.Resp3Text);
        BackButton = view.findViewById(R.id.BackButton);
        BackButton.setOnClickListener(this);
        updateUI(entry);
        versionCheck(version);

        return view;
    }

    public void updateUI(CVEntry entry){

        TitleTextField.setText(entry.getTitle());
        if(entry.getLocation() != null){
            LocationText.setText(entry.getLocation());
            LocationText.setVisibility(View.VISIBLE);
        }
        if(entry.getCompany() != null){
            WhereField.setText(entry.getCompany());
            WhereField.setVisibility(View.VISIBLE);
        }
        if(entry.getStart() != null){
            BeginningText.setText("• " + entry.getStart());
            EndText.setText(entry.getEnd());
            BeginningText.setVisibility(View.VISIBLE);
            EndText.setVisibility(View.VISIBLE);
        }
        if(entry.getResp() != null){
            TextView[] respArray = new TextView[]{Resp0Text, RespText1, RespText2, RespText3};
            for(int i = 0; i < entry.getResp().size(); i++){
                respArray[i].setText(entry.getRespVal(i));
                respArray[i].setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.BackButton){
            dismiss();
        }
    }

    public void versionCheck(String version) {
        if (version != null) {
            if (version.equals("Happy")) {
                layout.setBackgroundColor(0xFFFFFFB7);
            }
        }
    }
}
