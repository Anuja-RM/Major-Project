document.addEventListener('DOMContentLoaded', () => {

    // --- Smooth Scrolling for Anchor Links ---
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const targetElement = document.querySelector(this.getAttribute('href'));
            if (targetElement) {
                targetElement.scrollIntoView({
                    behavior: 'smooth'
                });
            }
        });
    });

    // --- Logic for Home Page (landing.html) ---
    const analyticsSection = document.querySelector('#analytics');
    if (analyticsSection) {
        const animateCounters = () => {
            const counters = document.querySelectorAll('.counter');
            counters.forEach(counter => {
                const target = counter.textContent;
                const numericValue = parseFloat(target.replace(/[^\d.]/g, ''));
                if (numericValue && numericValue > 1) {
                    let current = 0;
                    const increment = numericValue / 50; // Animation speed
                    const timer = setInterval(() => {
                        current += increment;
                        if (current >= numericValue) {
                            current = numericValue;
                            clearInterval(timer);
                        }
                        // Format the number back to its original style
                        if (target.includes('$')) {
                            counter.textContent = '$' + Math.floor(current).toLocaleString();
                        } else if (target.includes('%')) {
                            counter.textContent = Math.floor(current) + '%';
                        } else {
                            counter.textContent = Math.floor(current).toLocaleString();
                        }
                    }, 20); // Interval time
                }
            });
        };

        // Trigger counter animation when the section is visible
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    animateCounters();
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.5 }); // Trigger when 50% of the element is visible

        observer.observe(analyticsSection);
    }


    // --- Logic for Find Cafes Page (find_cafes.html / userFindCafe.html) ---
    const cafeGrid = document.getElementById('cafeGrid');
    if (cafeGrid) {
        const searchInput = document.getElementById('searchInput');
        const sortBySelect = document.getElementById('sortBy');

        const filterAndSortCafes = () => {
            const searchTerm = searchInput.value.toLowerCase();
            const sortBy = sortBySelect.value;
            const cafes = Array.from(cafeGrid.querySelectorAll('.cafe-card'));

            // Filter
            let visibleCount = 0;
            cafes.forEach(cafe => {
                const name = cafe.dataset.name.toLowerCase();
                const address = cafe.dataset.address.toLowerCase();
                const isVisible = name.includes(searchTerm) || address.includes(searchTerm);
                cafe.style.display = isVisible ? '' : 'none';
                if (isVisible) visibleCount++;
            });

            // Sort
            const sortedCafes = cafes.sort((a, b) => {
                switch (sortBy) {
                    case 'rating':
                        return parseFloat(b.dataset.rating) - parseFloat(a.dataset.rating);
                    case 'price':
                        return parseFloat(a.dataset.price) - parseFloat(b.dataset.price);
                    default: // 'name' or default
                        return a.dataset.name.localeCompare(b.dataset.name);
                }
            });

            // Re-append sorted cafes
            sortedCafes.forEach(cafe => cafeGrid.appendChild(cafe));

            // Update results count
            const resultsCountEl = document.getElementById('resultsCount');
            if(resultsCountEl) resultsCountEl.textContent = visibleCount;
        };

        if(searchInput) searchInput.addEventListener('input', filterAndSortCafes);
        if(sortBySelect) sortBySelect.addEventListener('change', filterAndSortCafes);

        // Initial call
        filterAndSortCafes();
    }

    // --- Logic for Cafe Details Page (userCafeDetails.html) ---
    const sortBySeatButton = document.getElementById('sort-by-seat');
    const sortByAvailabilityButton = document.getElementById('sort-by-availability');

    const sortPCs = (sortBy, event) => {
        const pcGrid = document.getElementById('pcGrid');
        if (!pcGrid) return;

        const pcCards = Array.from(pcGrid.querySelectorAll('.pc-card'));

        pcCards.sort((a, b) => {
            if (sortBy === 'availability') {
                const order = { 'Available': 0, 'Busy': 1, 'Full': 2 };
                const statusA = a.querySelector('.pc-status').textContent.trim();
                const statusB = b.querySelector('.pc-status').textContent.trim();
                return order[statusA] - order[statusB];
            } else { // Sort by seat number
                return parseInt(a.dataset.seat) - parseInt(b.dataset.seat);
            }
        });

        pcCards.forEach(card => pcGrid.appendChild(card));

        // Update button styles
        sortBySeatButton.classList.replace('btn-gaming', 'btn-secondary');
        sortByAvailabilityButton.classList.replace('btn-gaming', 'btn-secondary');
        event.target.classList.replace('btn-secondary', 'btn-gaming');
    }

    if (sortBySeatButton && sortByAvailabilityButton) {
        sortBySeatButton.addEventListener('click', (event) => {
            sortPCs('seat', event);
        });
        sortByAvailabilityButton.addEventListener('click', (event) => {
            sortPCs('availability', event);
        });
    }

});