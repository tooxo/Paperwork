package com.tschulte.paperwork;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AdvancedSearch {

    /*

    NOT YET USEABLE

     */

    static HashMap parseSearch(String search) {
        Pattern pattern = Pattern.compile("labels=\"(.+)\"");

        HashMap hm = new HashMap();

        List<String> lab = new ArrayList<>();

        if (search.matches(pattern.pattern())) {
            Matcher m = pattern.matcher(search);
            if (m.find()) {
                String labels = m.group();

                Pattern label = Pattern.compile("(.+?)(?:,|$)");
                Matcher lm = label.matcher(labels);
                while (lm.find()) {
                    lab.add(lm.group(1));
                }
            }
            search = search.replaceAll(pattern.pattern(), "");
            search = search.replaceAll(" {2,}", "");
            hm.put("search", search);
            hm.put("labels", lab);
            return hm;
        }
        hm.put("search", search);
        hm.put("labels", lab);
        return hm;
    }

    private String searchTerm = "";

    void showDialog(final Context activity, String search) {

        final Dialog dialog = new Dialog(activity);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.advanced_search);

        final EditText text = dialog.findViewById(R.id.text);
        final EditText labels = dialog.findViewById(R.id.labels);


        HashMap parsed = parseSearch(search);

        text.setText((String) parsed.get("search"));
        StringBuilder lbs = new StringBuilder();
        for (String o : ((List<String>) parsed.get("labels"))) {
            lbs.append(o).append(",").append(" ");
        }
        labels.setText(lbs.toString());


        Button ok = dialog.findViewById(R.id.okButton);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder st = new StringBuilder();

                String t = text.getText().toString();
                String l = labels.getText().toString();
                if (!t.equals("")) {
                    st.append('"').append(t).append('"').append(" ");
                }
                if (!l.equals("")) {
                    st.append("labels=").append('"').append(l).append('"');
                }
                setSearchTerm(st.toString());
                dialog.dismiss();
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) Math.round(activity.getResources().getDisplayMetrics().widthPixels * 0.9);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    void setSearchTerm(String newSearchTerm) {
        searchTerm = newSearchTerm;
    }

    String getSearchTerm() {
        return searchTerm;
    }

}
