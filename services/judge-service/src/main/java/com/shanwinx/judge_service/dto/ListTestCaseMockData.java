package com.shanwinx.judge_service.dto;

import java.util.List;
import java.util.UUID;

public final class ListTestCaseMockData {

    private ListTestCaseMockData() {
    }

    public static List<ListTestCaseDto> sample() {
        ListTestCaseDto testCase = ListTestCaseDto.builder()
                .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .problemId(UUID.fromString("111e8400-e29b-41d4-a716-446655440111"))
                .inputUrl("http://localhost:9001/api/v1/download-shared-object/aHR0cDovLzEyNy4wLjAuMTo5MDAwL3VwbG9hZHMvcHJvYmxlbXMvMTAwMDEvdGVzdGNhc2VzLzEvaW5wdXQuaW4_WC1BbXotQWxnb3JpdGhtPUFXUzQtSE1BQy1TSEEyNTYmWC1BbXotQ3JlZGVudGlhbD1XVkxROFNWNDdaT1lZTzRQMk5TUyUyRjIwMjYwNDA2JTJGdXMtZWFzdC0xJTJGczMlMkZhd3M0X3JlcXVlc3QmWC1BbXotRGF0ZT0yMDI2MDQwNlQwNjE3MTFaJlgtQW16LUV4cGlyZXM9NDMxOTkmWC1BbXotU2VjdXJpdHktVG9rZW49ZXlKaGJHY2lPaUpJVXpVeE1pSXNJblI1Y0NJNklrcFhWQ0o5LmV5SmhZMk5sYzNOTFpYa2lPaUpYVmt4Uk9GTldORGRhVDFsWlR6UlFNazVUVXlJc0ltVjRjQ0k2TVRjM05UUTVPVFF5TUN3aWNHRnlaVzUwSWpvaVlXUnRhVzRpZlEuT2Y5VXFqVkFtZk1wcHJWY2VYTDM3c2U1cUhCR1FvY01OZTZKMHZuWTZrYWlwaVRmWkpIZC1rWlNJS1NYZ0QwRU5xcVkyQkJaTGdkVkw4UFMta1JxcWcmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0JnZlcnNpb25JZD1udWxsJlgtQW16LVNpZ25hdHVyZT0xNjMwZTAzMjYwODY5NGUzNzUwNzQ4NWU0ZDRmMzFkMTQyZDk2NmUyMGIxOWZhM2Y2NzlmMzgxZTQzOGUyYTli")
                .outputUrl("http://localhost:9001/api/v1/download-shared-object/aHR0cDovLzEyNy4wLjAuMTo5MDAwL3VwbG9hZHMvcHJvYmxlbXMvMTAwMDEvdGVzdGNhc2VzLzEvb3V0cHV0Lm91dD9YLUFtei1BbGdvcml0aG09QVdTNC1ITUFDLVNIQTI1NiZYLUFtei1DcmVkZW50aWFsPVdWTFE4U1Y0N1pPWVlPNFAyTlNTJTJGMjAyNjA0MDYlMkZ1cy1lYXN0LTElMkZzMyUyRmF3czRfcmVxdWVzdCZYLUFtei1EYXRlPTIwMjYwNDA2VDA2MTc1MFomWC1BbXotRXhwaXJlcz00MzIwMCZYLUFtei1TZWN1cml0eS1Ub2tlbj1leUpoYkdjaU9pSklVelV4TWlJc0luUjVjQ0k2SWtwWFZDSjkuZXlKaFkyTmxjM05MWlhraU9pSlhWa3hST0ZOV05EZGFUMWxaVHpSUU1rNVRVeUlzSW1WNGNDSTZNVGMzTlRRNU9UUXlNQ3dpY0dGeVpXNTBJam9pWVdSdGFXNGlmUS5PZjlVcWpWQW1mTXBwclZjZVhMMzdzZTVxSEJHUW9jTU5lNkowdm5ZNmthaXBpVGZaSkhkLWtaU0lLU1hnRDBFTnFxWTJCQlpMZ2RWTDhQUy1rUnFxZyZYLUFtei1TaWduZWRIZWFkZXJzPWhvc3QmdmVyc2lvbklkPW51bGwmWC1BbXotU2lnbmF0dXJlPWQ1M2I1OWExYjc3MDEwYTY0MjYwMjczNWZmNjE0Mjk5NDdjOGZkNzFjMWM2ZmI0ZWFmOTMxODgyYTVhYzlhZTg")
                .problemTitle("Two Sum")
                .timeLimit(1000)
                .memoryLimit(256)
                .difficulty("EASY")
                .build();

        return List.of(testCase);
    }
}
