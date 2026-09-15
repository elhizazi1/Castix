document.addEventListener("DOMContentLoaded", () => {

    /* Navbar */

    const navbar = document.querySelector(".navbar");

    const updateNavbar = () => {
        if (!navbar) return;

        navbar.classList.toggle(
            "scrolled",
            window.scrollY > 20
        );
    };

    window.addEventListener("scroll", updateNavbar);
    updateNavbar();


    /* Scroll reveal */

    const elements = document.querySelectorAll(".reveal");

    const observer = new IntersectionObserver(
        entries => {

            entries.forEach(entry => {

                if (entry.isIntersecting) {
                    entry.target.classList.add("visible");
                    observer.unobserve(entry.target);
                }

            });

        },
        {
            threshold: 0.12
        }
    );

    elements.forEach(element => {
        observer.observe(element);
    });

});
