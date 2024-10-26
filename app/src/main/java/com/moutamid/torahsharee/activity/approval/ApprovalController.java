package com.moutamid.torahsharee.activity.approval;

import com.moutamid.torahsharee.databinding.ActivityApprovalBinding;

public class ApprovalController {

    public ApprovalActivity activity;
    public ActivityApprovalBinding b;

    public ApprovalController(ApprovalActivity activity, ActivityApprovalBinding b) {
        this.activity = activity;
        this.b = b;

//        b.galleryBtn.setOnClickListener(view -> {
//          toast("galleryBtn");
//            Intent i = new Intent();
//            i.setType("video/*");
//            i.setAction(Intent.ACTION_GET_CONTENT);
//            activity.startActivityForResult(Intent.createChooser(i, "choose App"), activity.CAMERA_PICK);
//        });

    }

}