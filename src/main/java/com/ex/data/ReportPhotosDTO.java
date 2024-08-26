package com.ex.data;

import java.time.LocalDateTime;

import com.ex.entity.DailyReportsEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class ReportPhotosDTO {				// 아직 미사용

	private Integer photoId;				// ReportPhotos 테이블 식별번호
    private DailyReportsEntity reportId;	// 알림장 식별번호
    private String filename;				// 파일이름
    private LocalDateTime uploadTime;		// 업로드일시
}
