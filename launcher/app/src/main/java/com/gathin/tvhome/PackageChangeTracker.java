package com.gathin.tvhome;

import android.annotation.TargetApi;
import android.content.pm.ChangedPackages;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Tracks package changes on API 26 and newer.
 * Older Android versions rely on MainActivity's package broadcast receiver.
 */
final class PackageChangeTracker {

    private PackageChangeTracker() {
    }

    static boolean changedSince(PackageManager pm, int sequence) {
        if (Build.VERSION.SDK_INT < 26) return false;
        return Api26.changedSince(pm, sequence);
    }

    static int currentSequence(PackageManager pm, int since) {
        if (Build.VERSION.SDK_INT < 26) return since;
        return Api26.currentSequence(pm, since);
    }

    @TargetApi(26)
    private static final class Api26 {

        static boolean changedSince(PackageManager pm, int sequence) {
            ChangedPackages changed = pm.getChangedPackages(sequence);
            return changed != null && !changed.getPackageNames().isEmpty();
        }

        static int currentSequence(PackageManager pm, int since) {
            ChangedPackages changed = pm.getChangedPackages(since);
            return changed != null ? changed.getSequenceNumber() : since;
        }
    }
}
