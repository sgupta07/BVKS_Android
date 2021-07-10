package com.iskcon.bvks.ui.about;

import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.iskcon.bvks.R;
import com.iskcon.bvks.util.ImageUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AboutFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(false);
        TabLayout tabs = getActivity().findViewById(R.id.tabs);
//        tabs.setVisibility(View.GONE);

        View root = inflater.inflate(R.layout.fragment_about, container, false);
        TextView prabhupadaTxt = root.findViewById(R.id.prabhupada_txt);
        Linkify.addLinks(prabhupadaTxt, Pattern.compile("His Divine Grace A.C. Bhaktivedanta Swami Prabhupada"), "http://bvks.com/about/srila-prabhupada/", (s, start, end) -> true, (match, url) -> "");
        prabhupadaTxt.setLinkTextColor(getResources().getColor(R.color.colorPrimaryDark));
        ImageView maharajImageView = root.findViewById(R.id.profile_image);
        ImageUtil.loadThumbnail(maharajImageView, R.drawable.about_screen_v4);
        TextView longAboutText = (TextView) root.findViewById(R.id.long_about_text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            longAboutText.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }
        return root;
    }
}