package xyz.victorolaitan.scholar.controller;

import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.model.subject.Subject;
import xyz.victorolaitan.scholar.fragment.FragmentActivity;
import xyz.victorolaitan.scholar.session.Session;
import xyz.victorolaitan.scholar.util.Nameable;

import static xyz.victorolaitan.scholar.fragment.FragmentId.COURSE_EDIT_FRAGMENT;
import static xyz.victorolaitan.scholar.fragment.FragmentId.SUBJECT_EDIT_FRAGMENT;

public class SubjectListCtrl implements FragmentCtrl {
    private Session parent;

    private FragmentActivity activity;
    private RecyclerAdapter subjectsAdapter;

    public List<SubjectCard> observableCards;

    public SubjectListCtrl(Session parent, FragmentActivity activity) {
        this.parent = parent;
        this.activity = activity;
    }

    public void init() {
        observableCards = new ArrayList<>();
    }

    @Override
    public void init(View view) {
        init();
    }

    @Override
    public void updateInfo() {
        observableCards.clear();
        expandList(-1, parent.getCalendar().getSubjects(), SubjectCard.SUBJECT);
    }

    public void setSubjectsAdapter(RecyclerAdapter subjectsAdapter) {
        this.subjectsAdapter = subjectsAdapter;
    }

    private <T extends Nameable> void expandList(int parentIndex, List<T> children, int childType) {
        for (int i = 1; i <= children.size(); i++) {
            observableCards.add(parentIndex + i,
                    new SubjectCard(childType, children.get(i - 1), parentIndex + i));
            subjectsAdapter.notifyItemInserted(parentIndex + i);
        }
        for (int i = parentIndex + children.size() + 1; i < observableCards.size(); i++) {
            observableCards.get(i).index = i;
        }
    }

    private void collapseList(int parentIndex, int size) {
        if (parentIndex + size >= observableCards.size())
            return;
        for (int i = 1; i <= size; i++) {
            observableCards.remove(parentIndex + 1);
            subjectsAdapter.notifyItemRemoved(parentIndex + 1);
        }
        for (int i = parentIndex + 1; i < observableCards.size(); i++) {
            observableCards.get(i).index = i;
        }
    }

    public void onNewSubjectClick() {
        activity.pushFragment(SUBJECT_EDIT_FRAGMENT, parent.getCalendar().newSubject(
                "Subject " + (parent.getCalendar().getSubjects().size() + 1),
                "SUBJ"));
    }

    public final class SubjectCard implements RecyclerCard {
        private static final int SUBJECT = 0;
        private static final int COURSE = 1;

        private int type;
        private Nameable object;
        private boolean expanded;
        private int index;

        private TextView txtName;
        private ImageButton expandBtn;

        private SubjectCard(int type, Nameable object, int index) {
            this.type = type;
            this.object = object;
            this.index = index;
        }

        @Override
        public void attachLayoutViews(View layout, CardView cv) {
            txtName = layout.findViewById(R.id.subjects_cardview_name);

            expandBtn = cv.findViewById(R.id.subjects_cardview_expand);
            if (type == COURSE) {
                layout.setPaddingRelative(0, 0, layout.getPaddingEnd(), 0);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMarginStart(30);
                layout.setLayoutParams(params);

                cv.setBackgroundColor(layout.getResources().getColor(R.color.colorBgSecondary));
                expandBtn.setVisibility(View.GONE);
                txtName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            } else {
                expandBtn.setOnClickListener(v -> SubjectCard.this.onExpandClick(expandBtn));
            }
            cv.setOnClickListener(v -> onClick());
        }

        @Override
        public void updateInfo() {
            txtName.setText(object.getName());
            if (type == SUBJECT) {
                expandBtn.setVisibility(
                        ((Subject) object).getCourseList().isEmpty() ? View.GONE : View.VISIBLE);
            }
        }

        private void onExpandClick(ImageView view) {
            if (!expanded) {
                expandList(index, ((Subject) object).getCourseList().toList(), COURSE);
                view.setImageDrawable(view.getResources().getDrawable(R.drawable.ic_expand_less_black_24dp));
            } else {
                collapseList(index, ((Subject) object).getCourseList().size());
                view.setImageDrawable(view.getResources().getDrawable(R.drawable.ic_expand_more_black_24dp));
            }
            expanded = !expanded;
        }

        /**
         * only called for subjects (i.e. control flow for courses never reaches here)
         */
        private void onClick() {
            if (type == COURSE) {
                activity.pushFragment(COURSE_EDIT_FRAGMENT, object);
            } else {
                activity.pushFragment(SUBJECT_EDIT_FRAGMENT, object);
            }
        }
    }
}
