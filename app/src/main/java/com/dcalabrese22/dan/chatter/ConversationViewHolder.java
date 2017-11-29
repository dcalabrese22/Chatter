package com.dcalabrese22.dan.chatter;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dcalabrese22.dan.chatter.helpers.GlideApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

//custom viewholder for a single conversation
public class ConversationViewHolder extends RecyclerView.ViewHolder {


    private TextView mSubject;
    private TextView mUser;
    private TextView mLastMessage;
    private ImageView mAvatar;
    private LinearLayout mLinearLayout;

    public ConversationViewHolder(View view) {
        super(view);
        mAvatar = view.findViewById(R.id.user_avatar);
        mSubject = view.findViewById(R.id.tv_conversation_subject);
        mUser = view.findViewById(R.id.tv_conversation_user);
        mLastMessage = view.findViewById(R.id.tv_conversation_last_message);
        mLinearLayout = view.findViewById(R.id.conversation_top);
    }

    //"flips" the users picture to show a checkmark when the conversation is selected
    public void flipAvatar(View v) {
        RelativeLayout container = v.findViewById(R.id.image_container);
        Object o = container.getTag();
        boolean isActivated = !o.equals("checked");
        v.setActivated(isActivated);

        final boolean isChecked = !o.equals("checked");
        if (isChecked) {
            container.setTag("checked");
        } else {
            container.setTag("unchecked");
        }

        v.findViewById(R.id.conversation_top).setSelected(true);
        final CircleImageView avatar = v.findViewById(R.id.user_avatar);

        final CircleImageView check = v.findViewById(R.id.user_message_checked);
        ObjectAnimator flipAvatarForwards = ObjectAnimator.ofFloat(avatar, "rotationY", 0f, 90f);
        final ObjectAnimator flipAvatarBack = ObjectAnimator.ofFloat(avatar, "rotationY", 90f, 0f);
        final ObjectAnimator flipCheckForwards = ObjectAnimator.ofFloat(check, "rotationY", 90f, 180f);
        final ObjectAnimator flipCheckBackwards = ObjectAnimator.ofFloat(check, "rotationY", 180f, 90f);
        flipCheckForwards.setDuration(150);
        flipAvatarBack.setDuration(150);
        flipAvatarForwards.setDuration(150);
        flipCheckBackwards.setDuration(150);

        flipAvatarForwards.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                flipCheckForwards.start();
                check.setVisibility(View.VISIBLE);
                avatar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        flipCheckBackwards.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                flipAvatarBack.start();
                check.setVisibility(View.GONE);
                avatar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        if (isChecked) {
            flipAvatarForwards.start();
        } else {
            flipCheckBackwards.start();
        }

    }

    public void setSubject(String subject) {
        mSubject.setText(subject);
    }

    public void setUser(String user) {
        mUser.setText(user);
    }

    public void setLastMessage(String message) {
        mLastMessage.setText(message);
    }

    public void setAvatar(Context context, String storagePath) {
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://"+storagePath);
        GlideApp.with(context)
                .load(reference)
                .into(mAvatar);
    }
}
