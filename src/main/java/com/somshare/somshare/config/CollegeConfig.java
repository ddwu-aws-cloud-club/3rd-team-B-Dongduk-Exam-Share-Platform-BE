package com.somshare.somshare.config;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 동덕여대 단과대학과 전공 매핑 정보
 */
@Component
public class CollegeConfig {

    private static final Map<String, List<String>> COLLEGE_MAJORS = new LinkedHashMap<>();

    static {
        COLLEGE_MAJORS.put("인문대학", Arrays.asList(
                "korean-literature", "korean-history", "creative-writing", "english",
                "japanese", "chinese", "european-studies", "korean-culture"
        ));

        COLLEGE_MAJORS.put("사회과학대학", Arrays.asList(
                "international-business", "economics", "library-info", "social-welfare", "child-studies"
        ));

        COLLEGE_MAJORS.put("경영대학", Arrays.asList("business-admin"));

        COLLEGE_MAJORS.put("자연정보과학대학", Arrays.asList(
                "food-nutrition", "health-management", "applied-chemistry", "cosmetics",
                "physical-education", "computer-science", "info-statistics"
        ));

        COLLEGE_MAJORS.put("약학대학", Arrays.asList("pharmacy"));

        COLLEGE_MAJORS.put("예술대학", Arrays.asList(
                "painting", "digital-craft", "curator", "piano", "orchestra", "vocal"
        ));

        COLLEGE_MAJORS.put("디자인이노베이션대학", Arrays.asList(
                "fashion-design", "visual-interior-design", "media-design", "fashion-design-night"
        ));

        COLLEGE_MAJORS.put("공연예술대학", Arrays.asList(
                "broadcasting", "practical-music", "dance", "model", "broadcasting-night"
        ));

        COLLEGE_MAJORS.put("문화지식융합대학", Arrays.asList(
                "communication-contents", "hci-science", "data-science",
                "culture-arts-management", "global-mice-fusion", "entrepreneurship"
        ));

        COLLEGE_MAJORS.put("미래인재융합대학", Arrays.asList(
                "tax-accounting", "financial-convergence"
        ));

        COLLEGE_MAJORS.put("ARETE 교양대학", Arrays.asList(
                "liberal-arts", "general-education", "teaching", "fashion-marketing",
                "global-multicultural", "social-big-data", "lifelong-education"
        ));
    }

    /**
     * 단과대학 이름으로 소속 전공 코드 목록 조회
     * @param collegeName 단과대학 이름
     * @return 전공 코드 목록 (없으면 빈 리스트)
     */
    public List<String> getMajorsByCollege(String collegeName) {
        return COLLEGE_MAJORS.getOrDefault(collegeName, Collections.emptyList());
    }

    /**
     * 단과대학 존재 여부 확인
     */
    public boolean isValidCollege(String collegeName) {
        return COLLEGE_MAJORS.containsKey(collegeName);
    }

    /**
     * 모든 단과대학 목록 조회
     */
    public Set<String> getAllColleges() {
        return COLLEGE_MAJORS.keySet();
    }
}
