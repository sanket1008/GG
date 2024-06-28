package com.unc.gearupvr.service


enum class API {

    ListColleges,
    Login,
    UserType,
    DashboardData,
    HighSchoolSearch,
    HighSchoolPost,
    MenuConfiguration,
    DetailPage,
    Majors,
    CollegeDetails,
    Careers,
    CareersDetails;

    internal var path: String
        get() {
            return when (this) {
                ListColleges -> "api/colleges/"
                Login -> "api/token/"
                UserType -> "api/user_types/"
                DashboardData -> "api/home_page/"
                HighSchoolSearch -> "/api/schools/"
                HighSchoolPost -> "/api/schools/"
                MenuConfiguration -> "/api/menus/"
                DetailPage -> "api/pages"
                Majors -> "api/majors/"
                CollegeDetails -> "api/colleges/"
                Careers -> "api/careers/"
                CareersDetails -> "api/careers/"
            }
        }
        set(_) {}

    internal var httpMethod: HttpMethod
        get() {
            return when (this) {
                ListColleges -> HttpMethod.POST
                Login -> HttpMethod.POST
                UserType -> HttpMethod.GET
                DashboardData -> HttpMethod.GET
                HighSchoolSearch -> HttpMethod.GET
                HighSchoolPost -> HttpMethod.POST
                MenuConfiguration -> HttpMethod.GET
                DetailPage -> HttpMethod.GET
                Majors -> HttpMethod.GET
                CollegeDetails -> HttpMethod.GET
                Careers -> HttpMethod.GET
                CareersDetails -> HttpMethod.GET
            }
        }
        set(_) {}

    internal var isSecured: Boolean
        get() {
            return when (this) {
                Login -> false
                else -> true
            }
        }
        set(_) {}
}
