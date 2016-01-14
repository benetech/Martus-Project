;The Martus(tm) free, social justice documentation and
;monitoring software. Copyright (C) 2001-2006, Beneficent
;Technology, Inc. (Benetech).

;Martus is free software; you can redistribute it and/or
;modify it under the terms of the GNU General Public License
;as published by the Free Software Foundation; either
;version 2 of the License, or (at your option) any later
;version with the additions and exceptions described in the
;accompanying Martus license file entitled "license.txt".

;It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
;IMPLIED, including warranties of fitness of purpose or
;merchantability.  See the accompanying Martus License and
;GPL license for more details on the required license terms
;for this software.

;You should have received a copy of the GNU General Public
;License along with this program; if not, write to the Free
;Software Foundation, Inc., 59 Temple Place - Suite 330,
;Boston, MA 02111-1307, USA.

!define LANG "VIETNAMESE" ; Required
;!insertmacro LANG_STRING <STRING_DEFINE> "string_value"

; language selection dialog stuff
!insertmacro LANG_STRING LangDialog_Title "Ngôn ngữ cài đặt"
!insertmacro LANG_STRING LangDialog_Text "Xin chọn ngôn ngữ cài đặt."

!insertmacro LANG_STRING FinishDialog_Text "${PRODUCT_NAME} ${PRODUCT_EXTENDED_VERSION} đã được cài đặt trên máy tính của bạn.\r\n \r\n Vào trang https://www.martus.org/ để xem có Gói Ngôn Ngữ Martus nào mới được cập nhật không. \r\n \r\nGói Ngôn Ngữ cho phép bạn cài đặt bất cứ lúc nào các bản dịch và tài liệu mới hoặc cập nhật, tiếp theo sau những lần phổ biến Martus trọn bộ. Gói Ngôn Ngữ có thể chứa bản dịch cập nhật của Giao Diện Người Dùng Martus, Cẩm Nang Sử Dụng, Cẩm Nang Bắt Đầu Nhanh, tập tin XEM TÔI, và trợ giúp của phần mềm.\r\n \r\nBấm nút Kết Thúc để đóng Thuật Sĩ Hỗ Trợ này."
!insertmacro LANG_STRING FinishDialog2_Text "."

; shortcuts
!insertmacro LANG_STRING StartMenuShortcutQuestion_Text "Bạn có cần cài đặt lối tắt vào Martus trong menu Bắt Đầu của Windows?"
!insertmacro LANG_STRING DesktopShortcutQuestion_Text "Bạn có cần cài đặt lối tắt vào Martus trên Bàn làm việc?"
!insertmacro LANG_STRING LaunchProgramInfo_Text "Lối tắt vào Martus được cài đặt trong thư mục chương trình $INSTDIR. Dùng lối tắt này, hoặc bản sao, để khởi động Martus."

!insertmacro LANG_STRING MartusShortcutDescription_Text "Hệ Thống Bản Tin Nhân Quyền Martus"

!insertmacro LANG_STRING MartusUserGuideShortcut_Text "Cẩm Nang Sử Dụng"
!insertmacro LANG_STRING MartusUserGuideShortcut_Filename "martus_user_guide.pdf"

!insertmacro LANG_STRING MartusQuickstartShortcut_Text "Bắt đầu nhanh"
!insertmacro LANG_STRING MartusQuickstartShortcut_Filename "quickstartguide.pdf"

!insertmacro LANG_STRING MartusUninstallShortcut_Text "Gỡ cài đặt"

; file property for .mba
!insertmacro LANG_STRING MartusMBAFileDesc_Text "Kho Lưu Trữ Bản Tin Martus"

; uninstall strings
!insertmacro LANG_STRING UninstallSuccess_Text "$(^Name) được xóa khỏi máy tính của bạn."

!insertmacro LANG_STRING NeedAdminPrivileges_Text "Bạn cần quyền hạn quản trị trong máy này mới có thể cài đặt $(^Name)."
!insertmacro LANG_STRING NeedAdminPrivilegesError_Text "Có lỗi khi tìm lấy quyền hạn quản trị. Kiểm lại xem bạn có quyền hạn quản trị trong máy không, nếu không thì việc cài đặt $(^Name) có thể không có kết quả."

!insertmacro LANG_STRING UninstallProgramRunning_Text "Xin kiểm lại là bạn đã rời $(^Name) bằng không trình tháo gỡ cài đặt sẽ không thể xóa bỏ các tập tin đang sử dụng."

!insertmacro LANG_STRING NewerVersionInstalled_Text "Có phiên bản mới ($EXISTING_MARTUS_VERSION) của ${PRODUCT_NAME} đã được cài đặt sẵn. Bạn phải tháo gỡ phiên bản hiện thời trước khi cài đặt phiên bản cũ hơn. Tuy nhiên nếu trở xuống phiên bản cũ thì bạn sẽ mất một số chức năng và có thể sẽ không xem được các bản tin được tạo ra trong phiên bản mới. Để giữ phiên bản mới, nhấn OK để rời trình cài đặt. Nếu bạn vẫn muốn trở xuống phiên bản cũ mặc dầu sẽ mất một số chức năng, rời trình cài đặt này, tháo gỡ phiên bản mới, rồi cài đặt lại phiên bản cũ."
!insertmacro LANG_STRING SameVersionInstalled_Text "Phiên bản hiện thời  ($EXISTING_MARTUS_VERSION) của ${PRODUCT_NAME} đã được cài đặt sẵn. Bạn muốn cài đặt lại?"
!insertmacro LANG_STRING UpgradeVersionInstalled_Text "Phiên bản cũ ($EXISTING_MARTUS_VERSION) của ${PRODUCT_NAME} đã được cài đặt sẵn. Trình cài đặt sẽ nâng cấp lên phiên bản ${PRODUCT_EXTENDED_VERSION}."
!insertmacro LANG_STRING RemoveInstallShieldVersion_Text "Có một phiên bản ${PRODUCT_NAME} cũ trước đây có trong máy tính. Chúng tôi sẽ tìm cách khởi động trình tháo gỡ cài đặt, và khi nó hoàn tất, trình cài đặt hiện thời sẽ tiếp tục. Nếu bạn chưa sao lưu chìa khóa trong phiên bản hiện thời của Martus, chúng tôi đề nghị bạn rời trình cài đặt này rồi làm sao lưu trước khi tháo gỡ cài đặt. Bạn có thể cho chạy trình cài đặt lại sau đó. Bạn có muốn tiếp tục với trình cài đặt này?"
!insertmacro LANG_STRING CannotUpgradeNoJava_Text "Phiên bản Martus đang có trong máy chỉ có thể được nâng cấp lên bằng trình cài đặt trọn bộ có chứa Java."
!insertmacro LANG_STRING CannotRemoveInstallShieldVersion_Text "Chúng tôi không thể tháo gỡ phiên bản Martus cũ trong máy. Trình cài đặt sẽ chấm dứt, xin tháo gỡ phiên bản cũ dùng lệnh Thêm/Gỡ Chương trình trong Pa-nen Điều khiển, rồi cho chạy lại trình cài đặt này. Nếu chưa làm sao lưu chìa khóa trong phiên bản hiện thời của Martus, chúng tôi đề nghị bạn nên làm trước khi gỡ cài đặt."
!insertmacro LANG_STRING CannotUpgradeNoMartus_Text "Đây là phiên bản nâng cấp của Martus. Xin vui lòng tải xuống và cho chạy trình cài đặt trọn bộ có chứa Java."

!insertmacro LANG_STRING UninstallMartusManuallyAndRemoveLinks_Text "Bạn không thể nâng cấp từ phiên bản Martus cũ hơn 4.3 lên phiên bản hiện thời.$\r$\n $\r$\nBạn phải gỡ cài đặt phiên bản Martus cũ (bằng cách chọn menu Bắt Đầu > Chương Trình > Martus > Gỡ cài đặt Martus, hoặc dùng Thêm/Gỡ Chương trình trong Pa-nen Điều khiển).$\r$\n $\r$\nNếu bạn sử dụng Windows Vista hoặc Windows 7 và có một lối tắt Martus trong menu Bắt Đầu, bạn có thể phải xóa nhóm Martus bằng cách vào menu Bắt Đầu > Chương Trình, bấm chuột nút phải vào nhóm Martus, và chọn 'Xóa'.$\r$\nNếu không các nối kết đó sẽ còn đó khi tháo gỡ phiên bản Martus hiện thời, đó có thể là một vấn đề an ninh."

