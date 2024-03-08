package com.techacademy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.service.ReportService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")
public class ReportController {

    private final ReportService reportService;
    private Employee employee;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // 日報一覧画面
    @GetMapping
    public String list(Model model ,@AuthenticationPrincipal UserDetail userDetail) {

        model.addAttribute("listSize", reportService.findAll(userDetail.getEmployee()).size());
        model.addAttribute("reportList", reportService.findAll(userDetail.getEmployee()));

        return "reports/list";
    }
    // 日報詳細画面
    @GetMapping(value = "/{id}/")
    public String detail(@PathVariable int id, Model model) {

        model.addAttribute("report", reportService.findById(id));
        return "reports/detail";
    }
    // 日報新規登録画面
    @GetMapping(value = "/add")
    public String create(@ModelAttribute Report report, @AuthenticationPrincipal UserDetail userDetail, Model model) {
     model.addAttribute("employeeName",userDetail.getEmployee().getName());
        return "reports/new";
    }

    // 日報新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Report report, BindingResult res, Model model, @AuthenticationPrincipal UserDetail userDetail) {

        // 入力チェック
        if (res.hasErrors()) {
            return create(null, userDetail, model);
        }

        // 論理削除を行った従業員番号を指定すると例外となるためtry~catchで対応
        // (findByIdでは削除フラグがTRUEのデータが取得出来ないため)
        try {
            ErrorKinds result = reportService.save(report,userDetail);

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create(null, userDetail, model);
            }

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DATECHECK_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DATECHECK_ERROR));
            return create(null, userDetail, model);
        }

        return "redirect:/reports";
    }
    // 日報更新画面
    @GetMapping(value = "/{id}/update")
    public String edit(@PathVariable int id, @ModelAttribute Report report, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        model.addAttribute("report", reportService.findById(id));
        //model.addAttribute("employeeName",userDetail.getEmployee().getName());
           return "reports/update";
       }
    // 日報更新処理
    @PostMapping(value = "/{id}/update")
    public String update(@PathVariable int id, @Validated Report report, BindingResult res, Model model, @AuthenticationPrincipal UserDetail userDetail) {

            // 入力チェック
            if (res.hasErrors()) {
                return edit(id, report, userDetail, model);
            }

            // 論理削除を行った従業員番号を指定すると例外となるためtry~catchで対応
            // (findByIdでは削除フラグがTRUEのデータが取得出来ないため)
            try {
                ErrorKinds result = reportService.renew(report,userDetail);

                if (ErrorMessage.contains(result)) {
                    model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                    return edit(id, report, userDetail, model);
                }

            } catch (DataIntegrityViolationException e) {
                model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DATECHECK_ERROR),
                        ErrorMessage.getErrorValue(ErrorKinds.DATECHECK_ERROR));
                return edit(id, report, userDetail, model);
            }

        return "redirect:/reports";
    }

// 日報削除処理
@PostMapping(value = "/{id}/delete")
public String delete(@PathVariable int id, Model model) {

    ErrorKinds result = reportService.delete(id);

    if (ErrorMessage.contains(result)) {
        model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
        model.addAttribute("report", reportService.findById(id));
        return detail(id, model);
    }

    return "redirect:/reports";
}

}