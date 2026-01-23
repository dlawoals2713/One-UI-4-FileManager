package com.dlawoals2713.oui4.file;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import androidx.window.embedding.ActivityFilter;
import androidx.window.embedding.ActivityRule;
import androidx.window.embedding.RuleController;
import androidx.window.embedding.SplitAttributes;
import androidx.window.embedding.SplitPairFilter;
import androidx.window.embedding.SplitPairRule;
import androidx.window.embedding.SplitPlaceholderRule;
import androidx.window.embedding.SplitRule;
import java.util.*;

class SplitManager {
    static void createSplit(Context context) {
        /*if (FileUtil.isExistFile(FileUtil.getPackageDataDir(context.getApplicationContext()).concat("/Smart all in one/data/unit_split.txt"))) {
            if (!FileUtil.readFile(FileUtil.getPackageDataDir(context.getApplicationContext()).concat("/Smart all in one/data/unit_split.txt")).equals("true")) {
                RuleController ruleController = RuleController.getInstance(context);
                ruleController.clearRules(); // 기존 Split rule 제거
                return;
            }
        }
        RuleController ruleController = RuleController.getInstance(context);

        // 공통 SplitAttributes 정의
        float ratio = 0.5f;
        SplitAttributes splitAttributes = new SplitAttributes.Builder()
                .setSplitType(SplitAttributes.SplitType.ratio(ratio))
                .setLayoutDirection(SplitAttributes.LayoutDirection.LEFT_TO_RIGHT)
                .build();

        // 1. HomeActivity - MemoActivity
        Set<SplitPairFilter> homeMemoFilters = new HashSet<>();
        homeMemoFilters.add(new SplitPairFilter(
                new ComponentName(context, SettingActivity.class),
                new ComponentName(context, TxtEditorActivity.class),
                null
        ));
        SplitPairRule homeMemoRule = new SplitPairRule.Builder(homeMemoFilters)
                .setDefaultSplitAttributes(splitAttributes)
                .setMinWidthDp(0)
                .setMinSmallestWidthDp(0)
                .setFinishPrimaryWithSecondary(SplitRule.FinishBehavior.NEVER)
                .setFinishSecondaryWithPrimary(SplitRule.FinishBehavior.ALWAYS)
                .setClearTop(false)
                .build();
        ruleController.addRule(homeMemoRule);


        Set<SplitPairFilter> updatelogFilters = new HashSet<>();
        updatelogFilters.add(new SplitPairFilter(
                new ComponentName(context, UpdatelogappActivity.class),
                new ComponentName(context, UpdatelogActivity.class),
                null
        ));
        SplitPairRule updatelogRule = new SplitPairRule.Builder(updatelogFilters)
                .setDefaultSplitAttributes(splitAttributes)
                .setMinWidthDp(0)
                .setMinSmallestWidthDp(0)
                .setFinishPrimaryWithSecondary(SplitRule.FinishBehavior.NEVER)
                .setFinishSecondaryWithPrimary(SplitRule.FinishBehavior.ALWAYS)
                .setClearTop(false)
                .build();
        ruleController.addRule(updatelogRule);

        // Placeholder: HomeActivity에서 PlaceholderActivity로 split
        ActivityFilter homePlaceholderActivityFilter = new ActivityFilter(
                new ComponentName(context, SettingActivity.class),
                null
        );
        // Placeholder: UpdatelogappActivity에서 PlaceholderActivity로 split
        ActivityFilter updatelogPlaceholderActivityFilter = new ActivityFilter(
                new ComponentName(context, UpdatelogappActivity.class),
                null
        );
        Set<ActivityFilter> placeholderActivityFilterSet = new HashSet<>();
        placeholderActivityFilterSet.add(homePlaceholderActivityFilter);
        placeholderActivityFilterSet.add(updatelogPlaceholderActivityFilter); // 추가됨

        SplitPlaceholderRule splitPlaceholderRule = new SplitPlaceholderRule.Builder(
                placeholderActivityFilterSet,
                new Intent(context, PlaceholderActivity.class)
        )
            .setDefaultSplitAttributes(splitAttributes)
            .setMinWidthDp(0)
            .setMinSmallestWidthDp(0)
            .setFinishPrimaryWithPlaceholder(SplitRule.FinishBehavior.ALWAYS)
            .build();
        ruleController.addRule(splitPlaceholderRule);

        Set<ActivityFilter> summaryActivityFilterSet = new HashSet<>();
        summaryActivityFilterSet.add(new ActivityFilter(new ComponentName(context, AboutActivity.class), null));
        summaryActivityFilterSet.add(new ActivityFilter(new ComponentName(context, UpdatelogActivity.class), null));

        ActivityRule activityRule = new ActivityRule.Builder(summaryActivityFilterSet)
                .setAlwaysExpand(true)
                .build();
        ruleController.addRule(activityRule);*/
    }
}