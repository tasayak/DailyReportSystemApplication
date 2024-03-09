package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }
    // 日報保存
    @Transactional
    public ErrorKinds save(Report report, @AuthenticationPrincipal UserDetail userDetail) {
        report.setEmployee(userDetail.getEmployee());

        //日報テーブルに、「ログイン中の従業員 かつ 入力した日付」の日報データが存在する場合はエラー
        if (reportRepository.existsByEmployeeAndReportDate(userDetail.getEmployee(), report.getReportDate())){

            return ErrorKinds.DATECHECK_ERROR;
        }

        report.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    // 日報更新
    @Transactional
    public ErrorKinds renew(Report report, @AuthenticationPrincipal UserDetail userDetail) {
        report.setEmployee(userDetail.getEmployee());

        Optional<Report> option =  reportRepository.findById(report.getId());
        Report oldReport = option.orElse(null);
        if ((!oldReport.getReportDate().equals(report.getReportDate())) && (reportRepository.existsByEmployeeAndReportDate(userDetail.getEmployee(), report.getReportDate()))){
            return ErrorKinds.DATECHECK_ERROR;
        }

        report.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    // 日報削除
    @Transactional
    public ErrorKinds delete(int id) {

        Report report = findById(id);
        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);
        report.setDeleteFlg(true);

        return ErrorKinds.SUCCESS;
    }

    // 日報一覧表示処理
    public List<Report> findByEmployee(Employee employee) {
        if (employee.getRole() == Employee.Role.ADMIN) {
        return reportRepository.findAll();

        }else {
            return reportRepository.findByEmployee(employee);
        }
    }

    // 1件を検索
    public Report findById(int id) {
        // findByIdで検索
        Optional<Report> option = reportRepository.findById(id);
        // 取得できなかった場合はnullを返す
        Report report = option.orElse(null);
        return report;
    }

}


