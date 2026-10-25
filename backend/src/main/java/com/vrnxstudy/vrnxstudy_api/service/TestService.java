package com.vrnxstudy.vrnxstudy_api.service;

import com.vrnxstudy.vrnxstudy_api.dto.test.*;
import java.util.List;
import java.util.UUID;

public interface TestService {

    TestResponse createTest(TestCreateRequest request, UUID creatorId);

    TestResponse getTest(UUID testId);

    List<TestResponse> getMyTests(UUID creatorId);

    TestQuestionResponse addQuestion(
            UUID testId,
            AddTestQuestionRequest request,
            UUID creatorId
    );

    TestResponse publishTest(UUID testId, UUID creatorId);

    void removeQuestion(UUID testId, UUID testQuestionId, UUID creatorId);
}