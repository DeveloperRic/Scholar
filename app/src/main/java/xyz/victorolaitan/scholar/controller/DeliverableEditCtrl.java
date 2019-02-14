package xyz.victorolaitan.scholar.controller;

import android.os.Build;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Calendar;
import java.util.UUID;

import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.fragment.FragmentActivity;
import xyz.victorolaitan.scholar.fragment.FragmentId;
import xyz.victorolaitan.scholar.model.subject.Deliverable;
import xyz.victorolaitan.scholar.session.DatabaseLink;
import xyz.victorolaitan.scholar.session.Session;
import xyz.victorolaitan.scholar.util.Schedule;
import xyz.victorolaitan.scholar.util.ScheduleChangeListener;
import xyz.victorolaitan.scholar.util.TextChangeListener;
import xyz.victorolaitan.scholar.util.Util;

public class DeliverableEditCtrl implements ModelCtrl {
    private Session parent;
    private FragmentActivity activity;
    private Deliverable deliverable;

    private TextView txtSubject;
    private TextView txtCourse;
    private TextView txtName;
    private TextView txtDate;
    private EditText txtEditName;
    private EditText txtEditDesc;
    private SeekBar seekPercent;

    public DeliverableEditCtrl(Session parent, FragmentActivity activity) {
        this.parent = parent;
        this.activity = activity;
    }

    public void init(View view) {
        txtSubject = view.findViewById(R.id.editDeliv_txtSubject);
        txtCourse = view.findViewById(R.id.editDeliv_txtCourse);
        txtName = view.findViewById(R.id.editDeliv_txtName);
        txtDate = view.findViewById(R.id.editSchedule_txtDate);

        txtEditName = view.findViewById(R.id.editDeliv_editName);
        txtEditName.addTextChangedListener((TextChangeListener) (s, start, before, count) -> {
            deliverable.setName(s.toString());
            txtName.setText(deliverable.getName());
        });

        txtEditDesc = view.findViewById(R.id.editDeliv_editDesc);
        txtEditDesc.addTextChangedListener(
                (TextChangeListener) (s, start, before, count) -> deliverable.setDescription(s.toString()));

        View.OnClickListener dateEditClickListener = v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTime(deliverable.getSchedule().getEnd());
            Util.newDatePicker(view, cal, deliverable.getSchedule(), Util.SCHEDULE_START, true).show();
        };
        view.findViewById(R.id.editSchedule_changeDate).setOnClickListener(dateEditClickListener);
        txtDate.setOnClickListener(dateEditClickListener);
        view.findViewById(R.id.editSchedule_makeRecurring).setOnClickListener(v ->
                activity.pushFragment(
                        FragmentId.SCHEDULE_EDIT_FRAGMENT,
                        new ScheduleEditCtrl.ScheduleView() {
                            @Override
                            public Schedule getSchedule() {
                                return deliverable.getSchedule();
                            }

                            @Override
                            public boolean postModel(DatabaseLink database) {
                                return DeliverableEditCtrl.this.postModel(database);
                            }
                        }));

        seekPercent = view.findViewById(R.id.editTest_seekPercent);
        seekPercent.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) deliverable.setOverallContribution(progress / 100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        deliverable.getSchedule().addChangeListener((ScheduleChangeListener) () -> this);
    }

    @Override
    public void duplicateModel() {
        deliverable.getEvaluation().newDeliverable(deliverable.getName(), deliverable.getDescription(), deliverable.getSchedule());
    }

    @Override
    public void deleteModel() {
        deliverable.getEvaluation().removeDeliverable(deliverable);
    }

    @Override
    public boolean postModel(DatabaseLink database) {
        return database.postEvaluation(deliverable.getEvaluation());
    }

    @Override
    public void updateInfo() {
        txtSubject.setText(deliverable.getEvaluation().getCourse().getSubject().getName());
        txtCourse.setText(deliverable.getEvaluation().getCourse().getName());
        if (deliverable.getName() != null)
            txtName.setText(deliverable.getName());
        txtDate.setText(Util.formatDate(deliverable.getSchedule()));
        txtEditName.setText(deliverable.getName());
        txtEditDesc.setText(deliverable.getDescription());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            seekPercent.setProgress((int) (deliverable.getOverallContribution() * 100), true);
        } else {
            seekPercent.setProgress((int) (deliverable.getOverallContribution() * 100));
        }
    }

    public Deliverable getDeliverable() {
        return deliverable;
    }

    public void setDeliverable(UUID testId) {
        setDeliverable((Deliverable) parent.getCalendar().search(testId));
    }

    public void setDeliverable(Deliverable aClass) {
        this.deliverable = aClass;
    }
}
